(ns reschedul.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [reschedul.layout :refer [error-page]]
            [reschedul.routes.home :refer [home-routes]]
            [reschedul.routes.services.services :refer [service-routes]]
            [reschedul.middleware :as middleware]
            [reschedul.db.core :as db]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [reschedul.config :refer [defaults]]
            [mount.core :as mount]
            ;[reschedul.routes.services.auth :as auth]
            ))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
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

(def app-routes
  (routes
    (var service-routes)
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
