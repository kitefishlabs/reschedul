(ns reschedul.middleware
  (:require [reschedul.layout :refer [*app-context* error-page]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.flash :refer [wrap-flash]]
            [immutant.web.middleware :refer [wrap-session]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.accessrules :refer [restrict wrap-access-rules]]
            [buddy.auth :refer [authenticated?]]
            [buddy.sign.jws :as jws]
            [buddy.core.keys :as ks]
            [clojure.java.io :as io]
            [reschedul.layout :refer [*identity*]]
            [reschedul.config :refer [defaults]]
            [reschedul.db.core :as db])
  (:import [javax.servlet ServletContext]
           (org.bson.types ObjectId)))

(defn wrap-context [handler]
  (fn [request]
    (binding [*app-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath ^ServletContext context)
                     (catch IllegalArgumentException _ context))
                ;; if the context is not specified in the request
                ;; we check if one has been specified in the environment
                ;; instead
                (:app-context env))]
      (handler request))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (timbre/error t)
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (wrap-restful-format handler {:formats [:json-kw :transit-json :transit-msgpack]}))

(defn on-error [request response]
  (error-page
    {:status 403
     :title (str "Access to " (:uri request) " is not authorized")}))

;(defn wrap-restricted [handler]
;  (restrict handler {:handler authenticated?
;                     :on-error on-error}))

;(defn wrap-identity [handler]
;  (fn [request]
;    (binding [*identity* (get-in request [:session :identity])]
;      (handler request))))

;(defn unsign-token [token]
;  (jws/unsign token (ks/public-key (io/resource "auth_pubkey.pem")) {:alg :rs256}))

;(defn wrap-config [handler]
;  (fn [req]
;    (handler (assoc req :auth-conf {:privkey "auth_privkey.pem"
;                                    :pubkey "auth_pubkey.pem"
;                                    :passphrase "9v4J3@0s<3"}))))

;(defn unsign-token [token]
;  (jws/unsign token (ks/public-key (io/resource "auth_pubkey.pem")) {:alg :rs256}))
;
;
;(defn wrap-auth-token [handler]
;  (fn [req]
;    (println (str ";;;; " req))
;    (println (str ";;;; " (-> req :session :token)))
;    (let [user (:user (when-let [token (-> req :session :token)]
;                        (unsign-token token)))]
;      (println (str "user: " user))
;      (handler (assoc req :auth-user user)))))

;(def rules
;  [{:uri "/api/users"
;    :handler authenticated?}
;   {:uri "/api/proposals"
;    :handler authenticated?}])

;(defn wrap-authentication [handler]
;  (fn [req]
;    (timbre/warn "auth-user? " (:auth-user req))
;    (if (:auth-user req)
;      (handler req)
;      {:status 302
;       :headers {"Location " (str "/login?m=" (:uri req))}})))


(defn wrap-print [handler label]
  (fn [req]
    (timbre/debug label req)
    (handler req)))

;(defn wrap-user [handler]
;  (fn [req]
;    (let [identity (get-in req [:user])]
;      (if (or (nil? identity) (= (:_id identity) ""))
;        (do
;          (timbre/debug "wrap-user-req no identity: " req)
;          (handler (assoc-in req [:session :identity] nil)))
;        (do
;          (timbre/debug "wrap-user-req: " req)
;          (handler (assoc-in req [:session :identity] identity)))))))

(defn wrap-user [handler]
  (fn [{userid :identity :as req}]
    (timbre/debug "userid: " userid)
    (if (not (nil? userid))
      (timbre/debug "userid: " (db/stringify_id (db/get-user-by-id userid))))
    (if (nil? userid)
      (handler req)
      (handler (assoc req :user (db/stringify_id (db/get-user-by-id userid)))))))


(def backend (session-backend))


(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      ;wrap-config
      wrap-formats
      wrap-webjars
      ;(wrap-print "0--->")
      wrap-user
      (wrap-authentication backend)
      (wrap-authorization backend)
      ;(wrap-print "1--->")
      (wrap-session {:cookie-attrs {:http-only true}})
      wrap-params
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            ;(dissoc :session)
            ))
      wrap-flash
      wrap-context
      wrap-internal-error))
