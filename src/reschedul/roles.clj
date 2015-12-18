(ns reschedul.roles)

(def reschedul-roles
  {:user 10 :organizer 20 :admin 30})

(defn any-granted? [req roles]
  (seq
    (clojure.set/intersection
      (set (map :role-id (-> req :auth-user :user-roles)))
      (set (vals (select-keys reschedul-roles roles))))))