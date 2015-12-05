(ns reschedul.pages.venues
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.util :refer [set-title!]]
            [ajax.core :refer [GET POST]]
            [reschedul.session :as session]))


(def state (reagent.core/atom { :editing? false :saved true :admin? false :owner? false}))

(defn loading-spinner []
  [:div.spinner
   [:div.rect1]
   [:div.rect2]
   [:div.rect3]
   [:div.rect4]
   [:div.rect5]])

;(def tooltip
;  ^{:component-did-mount #(.tooltip (js/$ (reagent.core/dom-node %)))}
;  (fn [message]
;    [:img.help {:src "img/help.png", :data-placement "bottom", :title message}]))

(defn row [id label]
  (fn []
    [:div.row.venue-row
     [:div.col-md-2 [:span label]]
     [:div.col-md-4 ^{:key label} [:span (str (session/get-in [:venue id]))]]
     [:div.col-md-3
      [:p "can't edit"]]
     [:div.col-md-3
      [:button.btn.btn-xs "flag"]
      [:button.btn.btn-xs "comment"]]]))

(defn edit_row [id label]
  ;(let [atom (atom {})]
  (fn []
    [:div.row.venue-edit-row
     [:div.col-md-2 [:span label]]
     [:div.col-md-5 ^{:key label} [:input {:type "text"
                                           :class "form-control"
                                           :value (session/get-in [:venue id])
                                           :on-change #(do (swap! state assoc-in [:saved] false)
                                                          (session/assoc-in! [:venue id] (-> % .-target .-value)))}]]
     [:div.col-md-2
    [:p "status"]]
     [:div.col-md-3
      [:button.btn.btn-xs {:on-click #()} "reset"]
      [:button.btn.btn-xs "verify"]
      [:button.btn.btn-xs "flag"]]]))

(defn venue-row [label id]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (:editing? @state)
        [edit_row id label]
        [row id label])]]))


(defn venues-page []
    ;(fn []
      (.log js/console (str "edit?? " @state))
      (set-title! (str "venue: " (session/get-in [:venue name])))
      [:div.row
       [:div.col-md-12
        [:div.row
         [:h2 "Welcome to the Buffalo Infringement Scheduling App and Website"]
         [:p "---"]]
          [:div.row
           [:input {:type "button"
                    :value (if (:editing? @state)
                             (str "edit")
                             (str "locked"))
                    :on-click #(swap! state update-in [:editing?] not)}]
           [:input {:type "button"
                    :value (if (:saved @state) "saved" "save?")
                    :on-click #(swap! state update-in [:saved] not)}]]
          [:div.row
           [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
            [venue-row "name" :name]
            [venue-row "nickname" :short_name]
            [venue-row "address" :address]
            [venue-row "description" :description]
            [venue-row "description_for_web" :description_for_web]
            [venue-row "latitude" :latitude]
            [venue-row "longitude" :longitude]
            [venue-row "owner" :owner]
            [venue-row "contact" :contact]
            [venue-row "infringement_contact" :infringement_contact]
            [venue-row "contact_phone" :contact_phone]
            [venue-row "contact_e-mail" :contact_e-mail]
            [venue-row "website" :website]
            [venue-row "phone" :phone]
            ;(when (and (session/get :admin)
            ;           (pos? (session/get-in [:post :id])))
            ;  [admin-forms])
            ]]]])