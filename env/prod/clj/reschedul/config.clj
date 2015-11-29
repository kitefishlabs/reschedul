(ns reschedul.config
  (:require [taoensso.timbre :as timbre]))

(def defaults
  {:init
   (fn []
     (timbre/info "\n-=[reschedul started successfully]=-"))
   :middleware identity})
