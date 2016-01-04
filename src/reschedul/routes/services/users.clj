(ns reschedul.routes.services.users
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]
            [reschedul.routes.services.auth :as auth]))

; Gather any social media (public) account info for possible integration and general publicity
; + will be private per user, external facing...
(s/defschema SocialInfo {;:_id                              s/Str
                         (s/optional-key :facebook-handle) s/Str
                         (s/optional-key :twitter-handle)  s/Str
                         (s/optional-key :website)         s/Str
                         (s/optional-key :soundcloud)      s/Str
                         (s/optional-key :vimeo)           s/Str
                         (s/optional-key :youtube)         s/Str
                         (s/optional-key :mailing-list)    s/Str
                         (s/optional-key :notes)           s/Str}) ;as in an artist's mailing list/fan club

; Non-public contact info
; + all contact info must be protected by auth/perms

(s/defschema ContactInfo {;:_id                                       s/Str
                          (s/optional-key :cell-phone)               s/Str
                          (s/optional-key :second-phone)             s/Str
                          (s/optional-key :email)                    s/Str
                          (s/optional-key :backup-email)             s/Str
                          (s/optional-key :address)                  s/Str
                          (s/optional-key :role)                     s/Str
                          (s/optional-key :preferred_contact_method) (s/enum :cell :email :second-phone)
                          (s/optional-key :notes)                    s/Str})

; basic user info and embedded contact + social info
(s/defschema User {:_id                          s/Str
                   :username                     s/Str
                   (s/optional-key :password)    s/Str
                   :first_name                   s/Str
                   :last_name                    s/Str
                   :admin                        s/Bool
                   :role                         s/Str
                   (s/optional-key :contact-info) ContactInfo
                   (s/optional-key :social-info) SocialInfo
                   (s/optional-key :notes)       s/Str})

(s/defschema UserStats {:unique-users s/Int
                        :admin-users s/Int
                        :unique-venues s/Int
                        :unique-proposals s/Int})

;(defroutes* users-public-routes
;            (context* ""
;                      :tags ["users"]
;                      (GET* "/users/stats"
;                            :return [UserStats]
;                            :summary "Just some data to demo a public route.")))

(defroutes* user-secure-routes
            (context* "" []
                      :tags ["users"]
                      (GET* "/users/stats" []
                            :return UserStats
                            :summary "Just some data to demo a public route."
                            (ok (db/get-users-stats)))
                      (GET* "/users" []
                            :return [User]
                            :summary "All usernames and __public__ data"
                            (ok (db/stringify_ids (db/get-all-users))))
                      (GET* "/users/:id" []
                            :path-params [id :- String]
                            :return User
                            :summary "User and its data"
                            (ok (db/stringify_id (db/get-user-by-id id))))
                      (GET* "/users/username/:username" []
                            :path-params [username :- String]
                            :return User
                            :summary "User and its data"
                            (ok (db/stringify_id (db/get-user-by-username username))))
                      (GET* "/users/email/:email" []
                            :path-params [email :- String]
                            :return [User]
                            :summary "User and its data"
                            (ok (db/stringify_ids (db/get-user-by-email email))))

                      (POST* "/users" []
                             :return User
                             :body [user (describe User "new user")]
                             :summary "new user, baby, yeah!"
                             (ok (db/stringify_id (db/user-create! user))))
                      ; + update the record
                      (POST* "/users/:id" []
                             :path-params [id :- String]
                             :return User
                             :body [user (describe User "update user")]
                             :summary "update user info"
                             (ok (db/stringify_id (db/user-update! user))))
                      (POST* "/users/:id/delete" []
                             :return User
                             :body [id :- String]
                             :summary "Delete the user account"
                             (ok (db/stringify_id (db/user-delete! id))))))




