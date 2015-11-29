(ns reschedul.config
  (:require [selmer.parser :as parser]
            [taoensso.timbre :as timbre]
            [reschedul.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (timbre/info "\n-=[reschedul started successfully using the development profile]=-"))
   :middleware wrap-dev})
