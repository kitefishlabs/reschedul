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

; db functions + atom

(defonce db (atom nil))

(defn connect! []
  ;; Tries to get the Mongo URI from the environment variable
  (reset! db (-> (:database-url env) mg/connect-via-uri :db)))

(defn disconnect! []
  (when-let [conn @db]
    (mg/disconnect conn)
    (reset! db nil)))

;
; Helpers
;

(defn stringify_ids [res]
  (map (fn [item]
         (assoc item :_id (str (:_id item)))) (seq res)))

(defn stringify_id [res]
  ;(println res)
  (assoc res :_id (str (:_id res))))


;
; Users
;

(defn get-all-users []
  (mq/with-collection
    @db
    "users"
    (mq/find { })
    (mq/sort {:name -1})))

(defn get-user [uname]
  (mc/find-one-as-map @db "users" {:username uname}))

(defn create-user [user]
  (mc/insert @db "users" user))

(defn update-user [id first-name last-name email]
  (mc/update @db "users" {:_id id}
             {$set {:first_name first-name
                    :last_name last-name
                    :email email}}))

(defn delete-user [user]
  (mc/remove @db "users" {:_id (:_id user)}))


;
; venues
;


(defn venues-all []
  (mq/with-collection
    @db
    "venues"
    (mq/find { })
    (mq/sort {:name -1})))

; return summaries of each venue
(defn venues-all-ids-names [with-location?]
  (let [fields (if with-location?
                 [:_id :name :active :short_name :latitude :longitude]
                 [:_id :name :active :short_name])]
    (mq/with-collection
      @db
      "venues"
      (mq/find { })
      (mq/fields fields)
      (mq/sort {:name -1}))))

;  just grabs the first one in the map - DEV + TESTING ONLY
(defn venues-one-example []
  (let [res (mq/with-collection
              @db
              "venues"
              (mq/find {})
              (mq/sort {:name -1})
              (mq/limit 1))]
    (timbre/info "res: " (first res))
    (first res)))

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
  (println (str "CREATE: " (merge x {:_id (ObjectId.)})))
  (let [newvenue (merge x {:_id (ObjectId.)})
        resp (mc/insert @db "venues" newvenue)]
    (timbre/log :warn (str resp))
    (if (acknowledged? resp)
      newvenue)))

(defn venue-update! [venue]
  (let [oid (:_id venue)]
    (println (str "Updating venue record: " oid))
    ;(mc/insert @db "venues" (merge venue {:_id (ObjectId.)})))
    (mc/save-and-return @db "venues" (merge venue {:_id (ObjectId. oid)}))))

  ;(defn venue-activate! [venue flag]
  ;  (let [oid (:_id venue)]
  ;    (println (str "Activating (or deactivating record) venue record: " oid))
  ;    ;(mc/insert @db "venues" (merge venue {:_id (ObjectId.)})))
  ;    (mc/save-and-return @db "venues" (merge venue {:active flag :_id (ObjectId. oid)}))))


(defn delete-venue! [x]
  (mc/remove @db "venues" x))




;
; SEED
;

(defn seed-database []
  (let [data-dir "/Users/kfl/dev/git/reschedul10/reschedul/seeddata"
        directory (clojure.java.io/file data-dir)
        files (file-seq directory)
        seed (load-all-seed-venues files)]

    (timbre/info "DB: " @db)
    ;(println (str "\n\n\n" seed "\n\n\n"))
    ;(println (str "\n\n\n" (count (hash-map seed)) "\n\n\n"))

    ;(timbre/debug "seed venues to insert: " (count seed))
    ;(timbre/info "seed venues to insert: " (first seed))
    ;(timbre/info "empty: " (mc/empty? @db "venues"))
    ; WARNING : everything stubbed fresh on each reset!
    (mc/remove @db "venues")
    (mc/remove @db "users")
    (mc/insert-batch @db "venues" seed)
    (create-user {:username "admin" :first_name "Ad" :last_name "Min" :admin true :email "tms@kitefishlabs.com" :pass "pass"})
    (create-user {:username "guestorganizer" :first_name "Fake" :last_name "Organizer" :admin true :email "tms@kitefishlabs.com" :pass "pass"})
    (create-user {:username "guestuser" :first_name "Faux" :last_name "User" :admin false :email "tms@kitefishlabs.com" :pass "pass"})))

