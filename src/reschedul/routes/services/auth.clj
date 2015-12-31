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
  (:import (org.bson.types ObjectId)))


(defn lookup-user [username password]
  ; get user by username, check password
  ; TODO: handle failures + tests
  (if-let [user (db/get-user-by-username username)]
    (do
      (timbre/debug "USER: " (str user "|" (:password user) "|" password))
      (if (hs/check password (:password user))
        (dissoc user :password))))) ; Strip out user password


(defn do-login [{{username :username password :password next :next} :body-params
                 session :session :as req}]
  ;(timbre/warn "u/p: " username)
  ;(timbre/warn "u/p: " password)
  ;(timbre/warn "sess: " session)
  (if-let [user (lookup-user username password)]
   (do
     (timbre/debug "user: " user)
     (assoc
       (response {:success true :user (db/stringify_id user)})
       :session
       (assoc session :identity (str (:_id user))))) ; Add  user id to the session
   (do
     (timbre/warn "login failed: "))))


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
                      ;(POST* "/register" []
                      ;       :body-params [username :- String, email :- String, password :- String]
                      ;       :summary "do register"
                      ;       do-register)
                      (GET* "/logout" []
                            do-logout)))