(ns reschedul.routes.services.auth
  (:require
    [ring.util.http-response :refer :all]
    [compojure.api.sweet :refer :all]
    [schema.core :as s]
    [reschedul.db.core :as db]

    ;[buddy.sign.generic :as sign]
    [buddy.sign.util :as buddyutil]
    [buddy.sign.jws :as jws]
    [buddy.core.keys :as ks]
    [clj-time.core :as t]
    [clojure.java.io :as io]

    [ring.util.response :as response]
    [clj-http.client :as http]
    [reschedul.views :as views]

    [buddy.hashers :as hs]
    [taoensso.timbre :as timbre]))

(defn show-login
  ([req] (show-login req nil))
  ([req errors]
   (views/layout {:request req
                  :title "Login"
                  :content (views/login req)
                  :errors errors})))

(defn auth-user [credentials]
  (let [user (db/get-user-by-username (:username credentials))
        unauthed [false {:message "Invalid username or password"}]]
    (if user
      (if (hs/check (:password credentials) (:password user))
        [true {:user (db/stringify_id (dissoc user :password))}]
        unauthed)
      unauthed)))

(defn- pkey [auth-conf]
  (ks/private-key
    (io/resource (:privkey auth-conf))
    (:passphrase auth-conf)))

(defn create-auth-token [auth-conf credentials]
  (let [[ok? res] (auth-user credentials)
        exp (-> (t/plus (t/now) (t/days 1)) (buddyutil/to-timestamp))]

    (if ok?
      [true {:token (jws/sign res
                              (pkey auth-conf)
                              {:alg :rs256 :exp exp})}]
      [false res])))


(defn create-token [req]
  (http/post "http://localhost:3000/api/create-auth-token"
             {:content-type :json
              :accept :json
              :throw-exceptions false
              :as :json
              :form-params (select-keys (:params req) [:username :password])}))

(defn do-login [req]
  (let [resp (create-token req)]
    (timbre/warn "(:status resp): " (:status resp))
    (condp = (:status resp)
      201 (-> (response/redirect (if-let [m (get-in req [:query-params "m"])] m "/#/users"))
              (assoc :session {:token (-> resp :body :token)}))
      401 (show-login req ["Invalid username or password"])
      {:status 500 :body "Something went pearshape when trying to authenticate"})))


(defn logout [req]
  (assoc (response/redirect "/") :session nil))