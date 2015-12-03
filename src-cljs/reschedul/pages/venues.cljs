(ns reschedul.pages.venues
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.util :refer [set-title!]]
            [ajax.core :refer [GET POST]]
            [reschedul.session :as session]))


;(defn fetch-venues []
;  (fn []
;    (GET "/api/venues"
;         {:handler #(do (set-current-venues! %)
;                        (set-venues-url))})))
;
;(defn select-venues-keys [venue]
;  (when venue
;    (select-keys venue [:name :address :phone :_id])))
;

(defn loading-spinner []
  [:div.spinner
   [:div.rect1]
   [:div.rect2]
   [:div.rect3]
   [:div.rect4]
   [:div.rect5]])

(def bar [:a "|"])

(defn venues-page []
  (let [{:keys [_id name short_name venue_type address description description_for_web latitude longitude owner contact infringement_contact contact_phone contact_e-mail website phone]}
        (session/get :venue)]
    (set-title! (str "venue: " name))
    (.log js/console (str (session/get :venue)))
    [:div.row
     [:div.col-md-12
      [:h2 "Welcome to the Buffalo Infringement Scheduling App and Website"]]
     (if _id
       [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
        [:div.entry-title [:h2 name ]]
        [:p address]
        [:p phone]
        ;(when (and (session/get :admin)
        ;           (pos? (session/get-in [:post :id])))
        ;  [admin-forms])
        [:div.entry-content
         ;[:div.post-content (markdown content)]
         ;[tags]
         ;[venue-nav]
         ;[:br]
         ;[comments]]]
         [loading-spinner]
         ;[sidebar]
         ]])]))
