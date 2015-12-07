(ns reschedul.db.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [monger.result :refer [acknowledged?]]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]

            [reschedul.seed.core :refer [load-all-seed-venues]])
  (:import (org.bson.types ObjectId)))


(defonce db (atom nil))

(defn connect! []
  ;; Tries to get the Mongo URI from the environment variable
  (reset! db (-> (:database-url env) mg/connect-via-uri :db)))

(defn disconnect! []
  (when-let [conn @db]
    (mg/disconnect conn)
    (reset! db nil)))

(defn create-user [user]
  (mc/insert @db "users" user))

(defn update-user [id first-name last-name email]
  (mc/update @db "users" {:_id id}
             {$set {:first_name first-name
                    :last_name last-name
                    :email email}}))

(defn get-user [id]
  (mc/find-one-as-map @db "users" {:_id id}))



(defn seed-venues []
  (let [data-dir "/Users/kfl/dev/git/reschedul10/reschedul/seeddata"
        directory (clojure.java.io/file data-dir)
        files (file-seq directory)
        seed (load-all-seed-venues files)]
    (timbre/info "DB: " @db)
    (timbre/info "seed venues to insert: " (count seed))
    (timbre/info "seed venues to insert: " (first seed))
    ;(timbre/info "empty: " (mc/empty? @db "venues"))
    ; WARNING : everything stubbed fresh on each reset!
    (mc/remove @db "venues")
    (mc/remove @db "users")
    (mc/insert-batch @db "venues" seed)
    (create-user {:first_name "Ad" :last_name "min" :email "tms@kitefishlabs.com" :pass "pass"})))


(defn transform_ids [res]
  (map (fn [item]
         (assoc item :_id (str (:_id item)))) (seq res)))

(defn transform_id [res]
  ;(println res)
  (assoc res :_id (str (:_id res))))

(defn venues-all []
  (mq/with-collection
    @db
    "venues"
    (mq/find {})
    (mq/sort {:name -1})))

;(defn venues-one []
;  (let [res (mq/with-collection
;              @db
;              "venues"
;              (mq/find {})
;              ;(mq/sort {:name -1})
;              (mq/limit 1))]
;    (timbre/info "res: " (first res))
;    (first res)))

(defn venues-all-pag [page per]
  (mq/with-collection
    @db
    "venues"
    (mq/find {})
    (mq/sort {:name -1})
    (mq/paginate :page (Integer. page) :per-page (Integer. per))))

(defn find-venue-by-id [idd]
  (mc/find-one-as-map @db "venues" {:_id (ObjectId. idd)}))

(defn venue-create! [x]
  ;(println (str "CREATE: " (merge x {:_id (ObjectId.)})))
  (let [newvenue (merge x {:_id (ObjectId.)})
        resp (mc/insert @db "venues" newvenue)]
    (if (acknowledged? resp)
      newvenue)))

(defn venue-update! [venue]
  (let [oid (:_id venue)]
    (println (str "oid: " oid))
    ;(mc/insert @db "venues" (merge venue {:_id (ObjectId.)})))
    (mc/save-and-return @db "venues" (merge venue {:_id (ObjectId. oid)}))))


(defn delete-venue! [x]
  (mc/remove @db "venues" x))

