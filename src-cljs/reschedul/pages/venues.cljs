(ns reschedul.pages.venues
  (:require [reagent.core :as r :refer [atom]]
            [reschedul.util :refer [set-title!]]
            [ajax.core :refer [GET POST]]
            [reschedul.session :as session]))

;(defn update-map [m f]
;  (reduce-kv (fn [m k v]
;               (assoc m k (f v))) {} m))

(defn strip-map-strings [m]
  (let [res (reduce-kv (fn [m k _]
                         (assoc m k "")) {} m)]
    ;(.log js/console (str "stripped resource:" res))
    res))

;(defonce todos (r/atom (sorted-map))) - ;
; TODO: sorted-map???!!!
(defonce state (r/atom { :editing? false :saved true :admin? false :owner? false :loaded false }))

(defn error-handler [resp] ; [{:keys [status status-text]}]
  (.log js/console
        (str "something bad happened: " resp))) ;" status " " status-text)))

(defn create-venue! [venue]
  (let [stripped-venue (strip-map-strings venue)
        empty-venue (assoc-in stripped-venue [:active] true)]
    (.log js/console (str empty-venue))
    (POST "/api/venue"
          {:params empty-venue
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "save-to-server success resp: " resp))
                      ;force the send of the venue to the server
                      ; TODO: -> add to recents!
                      (session/assoc-in! [:venue] resp)
                      (swap! state update-in [:saved] not))})))

(defn save-venue-to-server [venue]
  ;(let [empty-venue (strip-map-strings venue)]
    (POST "/api/venue"
          {:params venue
           :error-handler #(.log js/console "save-venue-to-server ERROR")
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "save-to-server success resp: " resp))
                      ;force the send of the venue to the server?
                      ; better - add to recents!
                      (session/assoc-in! [:venue] resp)
                      (swap! state update-in [:saved] not))}))


;(defn loading-spinner []
;  [:div.spinner
;   [:div.rect1]
;   [:div.rect2]
;   [:div.rect3]
;   [:div.rect4]
;   [:div.rect5]])



;(defn save-venue-to-server []
;  (POST "/api/venue" {:params (strip-map-strings (session/get :venue))
;                      :error-handler #(.log js/console "save-venue-to-server ERROR")
;                      :handler #(.log js/console (str "save-to-server success: " %))}))
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
      [:button.btn.btn-xs "+note"]
      [:button.btn.btn-xs "flag"]]]))

(defn venue-row [label id]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (:editing? @state)
        [edit_row id label]
        [row id label])]]))


(defn venues-page []
    (fn []
      (.log js/console (str "STATE " @state))

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
                  :on-click #(save-venue-to-server (session/get :venue))}]
         [:input {:type "button"
                  :value "new"
                  :on-click #(create-venue! (session/get :venue))}]]

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
          ]] ] ] ) )