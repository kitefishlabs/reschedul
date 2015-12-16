(ns reschedul.pages.venues
  (:require [clojure.string :refer [trim]]
            [reagent.core :as r :refer [atom dom-node]]
            [reschedul.util :refer [set-title! set-venues-url error-handler]]
            [ajax.core :refer [GET POST]]
            [reschedul.session :as session]))



; TODO: sorted-map???!!!
; state atom
(defonce state (r/atom { :editing? false :saved true :admin? false :owner? false :loaded false }))


;(defn update-map [m f]
;  (reduce-kv (fn [m k v]
;               (assoc m k (f v))) {} m))


(defn set-current-venue! [venue]
  ;(.log js/console (str "set venue!: " venue))
  (session/put! :venue venue)
  ;(set-recent!)
  (js/scroll 0 0))

(defn get-current-venue [id]
  (GET (str "/api/venue/" id)
       {:response-format :json
        :keywords? true
        :error error-handler
        :handler #(do (.log js/console "---> get current venue")
                      (set-current-venue! %))}))


(defn map-by-names [vs-info]
  (reduce
    (fn [coll x]
      (assoc-in coll [(keyword (:name x))] x))
    {}
    vs-info))

(defn collect-names [vs-info]
  (reduce
    (fn [coll x]
      (conj coll (:name x)))
    []
    vs-info))


(defn init-all-venues-info [venues-info]
  (.log js/console (str "set venue!: " venues-info))
  (session/put! :venues-info venues-info)
  (session/put! :venues-names-map (map-by-names venues-info))
  (session/put! :venues-names (collect-names venues-info))
  (.log js/console (str (session/get :venues-names-map)))
  )
; TODO: (set-recent!)



(defn create-venue! [venue]
  (let [stripped-venue (empty-all-string-values venue)
        empty-venue (assoc-in stripped-venue [:active] true)]
    (.log js/console (str empty-venue))
    (POST "/api/venue"
          {:params empty-venue
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "create-to-server success resp: " resp))
                      ;force the send of the venue to the server
                      ; TODO: -> add to recents!
                      (session/assoc-in! [:venue] resp)
                      (swap! state update-in [:saved] not))})))

(defn save-venue-to-server [venue]
    (POST (str "/api/venue/" (str (:_id venue)))
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


(defn row [id label]
  (fn []
    [:div.row.venue-row.xs
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

(defn trim-venue-name [vnames]
  (let [trimmed (map #(trim (str %)) vnames)]
    (.log js/console (str trimmed))
  trimmed))

(defn venues-did-mount []
  (.log js/console "venues did mount ----------->")
  (let [names (->> (session/get :venues-names-map)
                   keys
                   (map name)
                   (trim-venue-name))]
    (.log js/console (str "----------- names : " names))
    (js/$ (fn []
            (.autocomplete (js/$ "#venuesnames")
                           (clj->js {:source names}))))))


(defn venues-list-widget []
    (fn []
      (.log js/console "setup venues list ----------->")
      [(reschedul.util/mounted-component
         [:div.ui-widget
          [:label {:for "tags"} "Choose a venue: "]
          [:input#venuesnames]
          [:input {:type "button"
                   :value "load"
                   :on-click (fn []
                               (let [venue-key (-> (.getElementById js/document "venuesnames") .-value keyword)
                                     id (get-in (session/get :venues-names-map) [venue-key :_id])]
                                 ;(.log js/console (-> (.getElementById js/document "venuesnames") .-value keyword))
                                 (.log js/console id)
                                 (get-current-venue id)
                                 (.log js/console (str "get-current" (session/get-in [:venue])))
                                 ))}]]
         #(venues-did-mount))]))



(defn venues-page []

  (GET "/api/venues/info"
       {:response-format :json
        :keywords? true
        :handler #(do (.log js/console "\n\nWARNING\n-\nSHOULD NEVER SEE THIS POST-STARTUP!!!\n\n")
                      (init-all-venues-info %))})

  (GET "/api/venue"
       {:response-format :json
        :keywords? true
        :handler #(do (.log js/console "\n\nWARNING\n-\nSHOULD NEVER SEE THIS POST-STARTUP!!!\n\n")
                      (set-current-venue! %))})


  (.log js/console (str "STATE " @state))
  (fn []

      ;(.log js/console (str "VENUES-INFO " (session/get :venues-info)))

      (set-title! (str "venue: " (session/get-in [:venue :name])))
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
                  :on-click #(create-venue! (session/get :venue))}]
        [venues-list-widget]]

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
          [venue-row "phone" :phone]]]]]))