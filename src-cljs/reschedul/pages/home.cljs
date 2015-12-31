(ns reschedul.pages.home
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.util :refer [set-title!]]
            [reschedul.session :as session]))


(defn home-page []
  (fn []
    (let [user (session/get :user "NOBODY")]
      ;(.log js/console (str @session/state))
      (set-title! "HOME")
      [:div.row
       [:div.col-md-12
        [:div.row
         [:h2 "Dashboard"]
         [:p (str "Logged in: " (:username user))]]
        [:div.row
         [:div.col-md-6
          [:p "my user data"]
          [:p "contact data"]
          [:p "social data"]]
         [:div.col-md-6
          [:p "proposals"]
          [:p "availability"]
          [:p "mentions"]]]]])))