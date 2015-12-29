(ns reschedul.pages.home
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.util :refer [set-title!]]
            [reschedul.session :as session]))


(defn home-page []
  (fn []
    (let [user (session/get :user "NOBODY")]
      (.log js/console (str @session/state))
      (set-title! "HOME")
      [:div.row
       [:div.col-md-12
        [:h2 "Scheduling App and Website"]
        [:p (str "Logged in: " (:username user))]]])))