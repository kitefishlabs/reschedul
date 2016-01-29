(ns reschedul.db.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :as mq]
            [monger.result :refer [acknowledged?]]
            [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [reschedul.seed.core :refer [load-all-seed-venues]]
            [buddy.hashers :as hs])
  (:import (org.bson.types ObjectId)
           (com.mongodb ServerAddress MongoOptions)))

; db functions + atom

(defonce db (atom nil))

(defn connect! []
  ;; Tries to get the Mongo URI from the environment variable
  (timbre/debug (str "reset the db connection using uri: " (:database-url env)))
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
  (assoc res :_id (str (:_id res))))


;
; Users
;

(defn get-all-users []
  (let [fields [:username :full-name :email :role]]
    (mq/with-collection
      @db
      "users"
      (mq/find { })
      ; we could even expand the fields for admins...
      (mq/fields fields)
      (mq/sort (array-map :name -1)))))


(defn get-user-by-id [idd]
  (mc/find-one-as-map @db "users" {:_id (ObjectId. idd)}))

(defn get-user-by-username [uname]
  (mc/find-one-as-map @db "users" {:username uname}))

;(defn get-user-by-email [email]
;  (let [resp (mq/with-collection @db
;                                 "users"
;                                 (mq/find {:contact-info.email email})
;                                 (mq/sort (array-map :last_name -1)))]
;    (dissoc resp :password)))


(defn user-create! [user]
  ; merge the (blank!) user object with a new _id and encrypted password
  (let [newuser (merge user {:_id (ObjectId.)})
        hashed (update-in newuser [:password] #(hs/encrypt %))
        resp (mc/insert-and-return @db "users" hashed)]
    resp))

(defn user-update! [user]
  (let [oid (:_id user)]
    (mc/save-and-return @db "users" (merge user {:_id (ObjectId. oid)}))))


(defn user-delete! [userid]
  (mc/remove @db "users" {:_id userid}))



;
; venues
;

(defn venues-all []
  (mq/with-collection
    @db
    "venues"
    (mq/find { })
    (mq/sort (array-map :name -1))))

; return summaries of each venue
(defn venues-all-ids-names []
  ;(let [fields [:_id :name :active :short_name :latitude :longitude]]
  (let [fields [:_id :name]
        res (mq/with-collection
              @db
              "venues"
              (mq/find {})
              (mq/fields fields)
              (mq/sort (array-map :name -1)))]
    (println res)
    res))

;  just grabs the first one in the map - DEV + TESTING ONLY
;(defn venues-one-example []
;  (let [res (mq/with-collection
;              @db
;              "venues"
;              (mq/find {})
;              (mq/sort (array-map :name -1))
;              (mq/limit 1))]
;    ;(timbre/info "res: " (first res))
;    (first res)))

(defn venues-all-pag [page per]
  (mq/with-collection
    @db
    "venues"
    (mq/find {})
    (array-map :name -1)
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
; Proposals
;

(defn get-all-proposals []
  (mq/with-collection
    @db
    "proposals"
    (mq/find { })
    (mq/sort (array-map :title -1))))

;(defn get-users-stats []
;  {:unique-users (mc/count @db "users")
;   :admin-users (mc/count @db "users" {:admin true})
;   :unique-venues (mc/count @db "venues")
;   :unique-proposals (mc/count @db "propsal")})


(defn get-proposal-by-id [idd]
  (mc/find-one-as-map @db "proposals" {:_id (ObjectId. idd)}))

;(defn get-proposal-by-title [title]
;  (mc/find-one-as-map @db "proposals" {:title title}))

(defn get-proposals-for-user [username]
  (let [res (mq/with-collection
              @db
              "proposals"
              (mq/find {:proposer-username username})
              (mq/sort (array-map :title 1)))]
    (timbre/log :debug res)
    res))

(defn get-proposals-for-category [category]
  (mq/with-collection
    @db
    "proposals"
    (mq/find {:category category})
    (mq/sort (array-map :title 1))))


(defn proposal-create! [proposal]
  ; merge the (blank!) proposal object with a new _id
  (let [newproposal (merge proposal {:_id (ObjectId.)})
        resp (mc/insert @db "proposals"  newproposal)]
    (if (acknowledged? resp)
      newproposal))) ;take the default WRITE CONCERN (ACKNOWLEDGED)


(defn proposal-update! [proposal]
  (let [oid (:_id proposal)]
    (timbre/log :debug (str "Updating proposal record: " oid))
    (let [res (mc/save-and-return @db "proposals" (merge proposal {:_id (ObjectId. oid)}))]
      (timbre/log :debug res)
      res)))


(defn proposal-delete! [id]
  (mc/remove @db "proposals" {:_id id}))


;;; AVAILABILITIES
;
;(defn get-availability-info-by-id [idd]
;  (mc/find-one-as-map @db "availability-info" {:_id (ObjectId. idd)}))
;
;(defn get-availability-info-by-proposer-id [pid]
;  (mc/find-one-as-map @db "availability-info" {:proposer-id (ObjectId. pid)}))
;
;(defn availability-info-create! [availability-info]
;  ; merge the (blank!) availability-info object with a new _id
;  (let [new-availability-info (merge availability-info {:_id (ObjectId.)})
;        resp (mc/insert @db "availability-info"  new-availability-info)]
;    (timbre/log :debug "-------------------------->")
;    (timbre/log :debug new-availability-info)
;    (timbre/log :debug resp)
;    (if (acknowledged? resp)
;      new-availability-info)))
;;take the default WRITE CONCERN (ACKNOWLEDGED)
;
;(defn availability-info-update! [availability-info]
;  (let [oid (:_id availability-info)]
;    (println (str "Updating availability-info record: " oid))
;    (mc/save-and-return @db "availability-info" (merge availability-info {:_id (ObjectId. oid)}))))
;
;(defn availability-info-delete! [idd]
;  (mc/remove @db "availability-info" {:_id idd}))
;




;
; SEED
;

(defn seed-database []
  (let [data-dir (:seed-directory env)
        directory (clojure.java.io/file data-dir)
        files (file-seq directory)
        seed (load-all-seed-venues files)]

    (timbre/info "seed venues to insert: " (count seed))
    (timbre/info "DB: " @db)
    ;(println (str "\n\n\n" seed "\n\n\n"))
    ;(println (str "\n\n\n" (count (hash-map seed)) "\n\n\n"))

    ;(timbre/debug "seed venues to insert: " (count seed))
    ;(timbre/info "seed venues to insert: " (first seed))
    ;(timbre/info "empty: " (mc/empty? @db "venues"))
    ; WARNING : everything stubbed fresh on each reset!
    (mc/remove @db "venues")
    (mc/remove @db "users")
    (mc/remove @db "proposals")

    (let [response (mc/insert-batch @db "venues" seed)]
      (timbre/info (str "acknowledged?: " (acknowledged? response))))
    (timbre/info "seed venues to insert: " (count seed))
    (user-create! {:username "admin" :full-name "Ad M. In Sr." :role "admin" :password "password1" :email "tms@kitefishlabs.com"})
    (user-create! {:username "guestorganizer" :full-name "Fake Organizer" :role "organizer " :password "password2" :email "tms@kitefishlabs.com"})
    (user-create! {:username "guestuser" :full-name "Faux E. User" :role "user" :password "password3" :email "tms@kitefishlabs.com"})
    (proposal-create! {:primary-contact-name "Ad Min"
                       :proposer-username "admin"
                       :primary-contact-email "ad@min.net"
                       :primary-contact-phone "9990899"
                       :primary-contact-method "email"
                       :primary-contact-zipcode "14222"
                       :primary-contact-role "Band leader"
                       :title "BAND NAME HERE"
                       :category "music"
                       :assigned-organizer-username "admin"})
    (proposal-create! {:primary-contact-name "Ad E Min"
                       :proposer-username "admin"
                       :primary-contact-email "ad@min.net"
                       :primary-contact-phone "999-1111"
                       :primary-contact-method "phone"
                       :primary-contact-zipcode "14201"
                       :primary-contact-role "Best Boy Grip"
                       :title "FILM NAME HERE"
                       :category "film"
                       :assigned-organizer-username "admin"})
    (proposal-create! {:primary-contact-name "Yoo Ser"
                       :proposer-username "guestuser"
                       :primary-contact-email "guestuser@min.net"
                       :primary-contact-phone "9991111"
                       :primary-contact-method "cell"
                       :primary-contact-zipcode "14201"
                       :primary-contact-role "Troupe leader"
                       :title "THEATER PRODUCTION NAME HERE"
                       :category "theater"
                       :assigned-organizer-username "admin"})
    (proposal-create! {:primary-contact-name "Yoo Ser"
                       :proposer-username "guestuser"
                       :primary-contact-email "guestuser@min.net"
                       :primary-contact-phone "444-1111"
                       :primary-contact-method "email"
                       :primary-contact-zipcode "14225"
                       :primary-contact-role "Principal dancer"
                       :title "DANCE PERFORMANCE NAME HERE"
                       :category "dance"
                       :assigned-organizer-username "admin"})
    (proposal-create! {:primary-contact-name "Ad E Min"
                       :proposer-username "admin"
                       :primary-contact-email "ad@min.net"
                       :primary-contact-phone "999-1111"
                       :primary-contact-method "phone"
                       :primary-contact-zipcode "14222"
                       :primary-contact-role "Poet"
                       :title "SPOKEN WORD NAME HERE"
                       :category "spokenword"
                       :assigned-organizer-username "admin"})
    (proposal-create! {:primary-contact-name "F. Org"
                       :proposer-username "guestorganizer"
                       :primary-contact-email "guestorg@min.net"
                       :primary-contact-phone "999-1211"
                       :primary-contact-method "email"
                       :primary-contact-zipcode "14201"
                       :primary-contact-role "Artisté"
                       :title "FILM NAME HERE"
                       :category "visualart"
                       :assigned-organizer-username "admin"})))