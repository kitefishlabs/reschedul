(ns reschedul.handler
  (:require ;[compojure.core :refer :all]
            [reschedul.layout :refer [error-page]]
            [reschedul.routes.home :refer [home-routes]]
            [reschedul.middleware :as middleware]
            [ring.util.http-response :refer [ok]]
            ;[reschedul.db.core :as db]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            ;[selmer.parser :as parser]
            [environ.core :refer [env]]
            [reschedul.config :refer [defaults]]
            [mount.core :as mount]
            [compojure.api.sweet :refer :all] ;[defapi swagger-docs context* routes]]
            [buddy.auth.accessrules :refer [restrict]]
            [reschedul.routes.services.auth :as auth]
            [reschedul.routes.services.users :as users]
            [reschedul.routes.services.venues :as venues]
            [reschedul.routes.services.proposals :as proposals]
            ;[reschedul.routes.services.proposal_info :as proposal-info]
            ;[reschedul.routes.services.availability_info :as availability-info]
            ))

(defn init
  "init will be called once when
   app is deployed as a servlet.
   put any initialization code here"
  []

  (timbre/merge-config!
    {:level     ((fnil keyword :info) (env :log-level))
     :appenders {:rotor (rotor/rotor-appender
                          {:path (or (env :log-path) "reschedul.log")
                           :max-size (* 512 1024)
                           :backlog 10})}})
  (mount/start)
  ((:init defaults)))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "reschedul is shutting down...")
  (mount/stop)
  (timbre/info "shutdown complete!"))


(defn on-error
  [request value]
  {:status 403
   :headers {}
   :body { :error "Not authorized"}})

;(defroutes* stub-secure-routes
;            (context* "" []
;                      :tags ["stub"]
;                      (GET* "/this-is-bullshit" []
;                            :return String
;                            :summary "BSBSBSBS"
;                            (ok (str "It is indeed bullshit.")))))

(defapi service-routes
        ;(ring.swagger.ui/swagger-ui "/swagger-ui")
        ;JSON docs available at the /swagger.json route
        ;(swagger-docs :title "Reschedul api")
        (context* "/api" []
                  :tags [:api]
                  auth/auth-routes
                  (restrict users/user-secure-routes {:handler  auth/is-authenticated?
                                                      :on-error on-error})
                  (restrict venues/venue-secure-routes {:handler  auth/is-authenticated?
                                                        :on-error on-error})
                  (restrict proposals/proposal-secure-routes {:handler  auth/is-authenticated?
                                                              :on-error on-error})
                  ;(restrict proposal-info/proposal-info-secure-routes {:handler  auth/is-authenticated?
                  ;                                                     :on-error on-error})
                  ;(restrict availability-info/availability-info-secure-routes {:handler  auth/is-authenticated?
                  ;                                                     :on-error on-error})
                  ))


(defroutes* app-routes
            service-routes
            (wrap-routes #'home-routes middleware/wrap-csrf)
            (route/not-found
              (:body
                (error-page {:status 404
                             :title "page not found"}))))


(def app (middleware/wrap-base #'app-routes))
