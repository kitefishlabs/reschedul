(ns reschedul.pages.home
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.util :refer [set-title!]]
            [reschedul.session :as session]))


(defn home-page []
  ;(let [{:keys [_id name short_name venue_type address description description_for_web latitude longitude owner contact infringement_contact contact_phone contact_e-mail website phone]}
  ; (session/get :venue)]
  (set-title! "HOME")
  [:div.row
   [:div.col-md-12
    [:h2 "Scheduling App and Website"]
    [:p "Welcome! Soon there will be a dashboard here!"]]])