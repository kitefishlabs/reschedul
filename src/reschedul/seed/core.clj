(ns reschedul.seed.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [difference union]]
    ;[clj-commons-exec :as exec]
    ;[net.cgrand.enlive-html :as html]
    ;[clj-http.client :as client]
    ;[clj-http.cookies :as cookies]
            [cheshire.core :refer [decode]]))

(def whitelist-set
  #{:name :short_name :address :phone
    :venue_type :description :description_for_web
    :wall_size :wall_space
    :prefered_genre_primary :prefered_genre_secondary :performances_allowed :schedule_availability :best_performances
    :latitude :longitude
    :owner :contact :infringement_contact :contact_e-mail :contact_phone :website})

;(def directory (clojure.java.io/file "/Users/kfl/dev/git/reschedul9/data"))
;(def files (file-seq directory))

;(defn build-venue-json-filename [root venueid]
;  (str root venueid ".json"))

(defn read-json-file [fullpath]
  (let [slurped (decode (slurp fullpath))]
    slurped))

(defn filter-whitelisted [venue_]
  (reduce-kv
    (fn [m k v]
      (if
        (nil? (whitelist-set (keyword k)))
        m
        (assoc m k v)))
    {}
    venue_))

;(filter-whitelisted (read-json-file "/Users/kfl/dev/git/reschedul9/data/" 9600))

(defn load-all-seed-venues [files]
  (println (str "load-all-seeds"))
  (map
    (fn [fullpath]
      (filter-whitelisted (read-json-file fullpath)))
    (nthrest (map str files) 2)))


;(def sanitized-venues (load-all-seed-venues))

;sanitized-venues
