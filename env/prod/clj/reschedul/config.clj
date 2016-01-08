(ns reschedul.config
  (:require [taoensso.timbre :as timbre]
            [environ.core :refer [env]]))

(def defaults
  {:init
   (fn []
     (timbre/info "\n-=[reschedul started successfully]=-")
     (timbre/info (str "db: " (:database-url env))))
   :middleware identity})
