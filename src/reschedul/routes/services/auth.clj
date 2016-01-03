(ns reschedul.routes.services.auth
  (:require
    [ring.util.http-response :refer :all]
    [compojure.api.sweet :refer :all]
    [schema.core :as s]
    [reschedul.db.core :as db]

    ;[buddy.sign.generic :as sign]
    [buddy.sign.util :as buddyutil]
    [buddy.sign.jws :as jws]
    [buddy.core.keys :as ks]
    [clj-time.core :as t]
    [clojure.java.io :as io]

    [ring.util.response :refer [response redirect]]
    [clj-http.client :as http]
    [reschedul.views :as views]

    [buddy.hashers :as hs]
    [taoensso.timbre :as timbre])
  (:import (org.bson.types ObjectId)
           (clojure.lang Keyword)))

; TODO: security --> this HAS TO split on authed orgs - seeing all data - vs unauthed users - seeing just public data

(defn lookup-user [username]
  ; get user by username, check password
  ; TODO: handle failures + tests
  (if-let [user (db/get-user-by-username username)]
    (dissoc user :password)))

(defn lookup-user-pass [username password]
  ; get user by username, check password
  ; TODO: handle failures + tests
  (if-let [user (db/get-user-by-username username)]
    (do
      (timbre/debug "USER LOOKUP: " (str user "|" (:password user) "|" password))
      (if (hs/check password (:password user))
        (dissoc user :password))))) ; Strip out user password


(defn do-login [{{username :username password :password next :next} :body-params
                 session :session :as req}]
  ;(timbre/warn "u/p: " username)
  ;(timbre/warn "u/p: " password)
  ;(timbre/warn "sess: " session)
  (if-let [user (lookup-user-pass username password)]
   (do
     (timbre/debug "user: " user)
     (assoc
       (response {:success true :user (db/stringify_id user)})
       :session
       (assoc session :identity (str (:_id user))))) ; Add  user id to the session
   (do
     (timbre/warn "login failed: "))))

(defn do-register [{{username :username
                     email :email
                     first_name :first_name
                     last_name :last_name
                     admin :admin
                     role :role
                     password1 :password1
                     password2 :password2} :body-params ;next :next
                 session :session :as req}]

  (if-let [user (lookup-user username)]
    (do
      (timbre/debug "user: " user)
      (assoc
        (response {:success false :reason :exists :user (db/stringify_id user)})
        :session
        (assoc session :identity (str (:_id user))))))

  ; doesn't exist, create new!
  (if (= password1 password2)
    (let [newly-created (db/stringify_id
                          (db/user-create! {:username username
                                            :contact-info {:email email}
                                            :first_name first_name
                                            :last_name last_name
                                            :admin admin
                                            :role role
                                            :password password1}))]
      (if-not (nil? newly-created)
        (do
          (timbre/debug "newly-created: " newly-created)
          (assoc
            (response {:success true :user newly-created })
            :session
            (assoc session :identity (str (:_id newly-created)))))
        (response {:success false :user nil}))))) ; Add  user id to the session




(defn do-logout [{session :session}]
  (assoc
    (response "")                           ; Redirect to login
    :session
    (dissoc session :identity))) ; Remove :identity from session

(defn is-authenticated? [{user :user :as req}]
  (not (nil? user)))


(defroutes* auth-routes
            (context* "/auth" []
                      :tags ["auth"]
                      (POST* "/login" []
                             :body-params [username :- String, password :- String]
                             :summary "do login"
                             do-login)
                      (POST* "/register" []
                             :body-params [username :- String, first_name :- String, last_name :- String, email :- String, admin :- Boolean, role :- Keyword, password1 :- String, password2 :- String]
                             :summary "do register"
                             do-register)
                      (GET* "/logout" []
                            do-logout)))