(ns reschedul.pages.venues
  (:require [clojure.string :refer [trim]]
            [reagent.core :as r :refer [atom dom-node]]
            [secretary.core :as secretary :include-macros true]
            [reschedul.util :refer [set-title!
                                    set-venues-url
                                    error-handler
                                    empty-all-string-values
                                    trim-list-of-strings]]
            [ajax.core :refer [GET POST]]
            [reschedul.session :as session]
            [reschedul.pages.common :refer [control-row hints-pane schema-row schema-textarea-row schema-boolean-row schema-dropdown-row]]))



; TODO: sorted-map???!!!
; state atom
(defonce state-atom (r/atom { :editing? false :saved true })) ;:admin? false :owner? false :loaded false


;(defn update-map [m f]
;  (reduce-kv (fn [m k v]
;               (assoc m k (f v))) {} m))


;(defn set-current-venue! [venue]
;  ;(.log js/console (str "set venue!: " venue))
;  (session/put! :venue venue)
;  ;(set-recent!)
;  (js/scroll 0 0))

;(defn get-current-venue [id]
;  (GET (str "/api/venue/" id)
;       {:response-format :json
;        :keywords? true
;        :error error-handler
;        :handler ;#(do (.log js/console "---> get current venue")
;                      #(set-current-venue! %)}))


;(defn map-by-names [vs-info]
;  (reduce
;    (fn [coll x]
;      (assoc-in coll [(keyword (:name x))] x))
;    {}
;    vs-info))
;
;(defn collect-names [vs-info]
;  (reduce
;    (fn [coll x]
;      (conj coll (:name x)))
;    []
;    vs-info))


(defn init-all-venues-info [venues-info]
  (.log js/console (str "set venues info: " venues-info))
  (session/put! :venues-names-map venues-info)
  ;(session/put! :venues-names (collect-names venues-info))
  (.log js/console (str "\n\n-->\n" (session/get :venues-names-map)))
  )


(defn create-venue-on-server! []
  (let [empty-venue {:name "" :nickname "" :address "" :description "" :description_for_web ""
                     :latitude "" :longitude "" :owner "" :contact "" :contact_phone ""
                     :contact_e-mail "" :infringement_contact "" :website "" :phone "" :active true}]
    (.log js/console (str empty-venue))
    (POST "/api/venue"
          {:params empty-venue
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "create-to-server success resp: " resp))
                      (session/assoc-in! [:current-venue] resp)
                      (swap! state-atom update-in [:saved] not))})))

(defn save-venue-to-server! [venue]
  (POST (str "/api/venue/" (:_id venue))
        {:params venue
         :error-handler #(.log js/console "save-venue-to-server ERROR")
         :response-format :json
         :keywords? true
         :handler (fn [resp]
                    (.log js/console (str "save-to-server success resp: " resp))
                    (session/assoc-in! [:current-venue] resp)
                    (swap! state-atom update-in [:saved] not))}))

(defn get-venue-from-server! [id]
  (GET (str "/api/venue/" id)
       {:error-handler #(.log js/console "get-venue-from-server ERROR")
        :response-format :json
        :keywords? true
        :handler (fn [resp]
                   (.log js/console (str "get-from-server success resp: " resp))
                   (session/assoc-in! [:current-venue] resp)
                   (swap! state-atom update-in [:saved] not))}))


;; TODO: this needs to be wrapped by auth
(defn logged-in-user-venues-list []
  (let [venues (session/get-in [:venues-names-map])]
    (fn []
      [:div.panel.panel-default
      [:div.panel-heading
        [:h4 (str "Infringement Venues")]
       [control-row state-atom]
       [hints-pane]]
       [:div.panel-body
        [:div.col-md-12
         (.log js/console (count venues))
         (for [ven venues]
           [:div.row
            ^{:key (:_id ven)}
            [:div.col-md-4 [:span (str (:name ven))]]
            [:div.col-md-6 [:a { :on-click #(do
                                             (.log js/console (str "/venues/" (:_id ven)))
                                             (get-venue-from-server! (:_id ven))
                                             (secretary/dispatch! (str "/venues/" (:_id ven))))} "view" ]]
            [:div.col-md-2 [:span "end-cap"]]])]]])))

;; TODO: this needs to be wrapped by auth
(defn logged-in-user-venues-display []
  ;(let [edit? (r/atom true)]
  (fn []
    ;(.log js/console (str @session/state))
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Basic information")]
      [control-row state-atom]
      [hints-pane]]
     [:div.panel-body
      [:div.col-md-12
       [:input {;:name "create"
                :type "button"
                :value "create"
                :on-click #(create-venue-on-server! )}]
       [schema-row "Name" "Official venue name" [:current-venue :name ] state-atom]
       [schema-row "Nickname" "e.g. \"The Pink\"" [:current-venue :short-name] state-atom]
       [schema-row "Address" "123 Spoon St., Buffalo, NY 14201 " [:current-venue :address] state-atom]
       [schema-row "Description" "Describe the venue's available performance/gallery space. Be as detailed as you can." [:current-venue :description] state-atom]
       [schema-row "Description for web" "One sentence description." [:current-venue :description-for-web] state-atom]
       [schema-row "Latitude" "We can fill this in if you can't." [:current-venue :latitude] state-atom]
       [schema-row "Longitude" "We can fill this in if you can't." [:current-venue :longitude] state-atom]
       [schema-row "Owner" "Just so we know." [:current-venue :owner] state-atom]
       [schema-row "Contact" "The person we actually interface with." [:current-venue :contact-name] state-atom]
       [schema-row "Contact phone" "Preferably a cell phone or the biz phone." [:current-venue :contact-phone] state-atom]
       [schema-row "Contact e-mail" "Also important, even if an organizer/performer is acting as contact." [:current-venue :contact-e-mail] state-atom]
       [schema-row "Infringement contact" "Or we assign a festival contact." [:current-venue :infringement-contact] state-atom]
       [schema-row "Website" "Official business site or social media page." [:current-venue :website] state-atom]
       [schema-row "Phone" "Listed business phone number." [:current-venue :phone] state-atom]]]]))



(defn venues-did-mount []
  (let [names (->> (session/get :venues-names-map)
                   keys
                   (map name)
                   (trim-list-of-strings))]
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
                                 (get-venue-from-server! id)))}]]
         #(venues-did-mount))]))


(defn venue-detail-page []
  ; if no current info map, then get it
  (if (nil? (session/get :venues-info))
    (GET "/api/venue/names"
         {:response-format :json
          :keywords? true
          :handler #(do (.log js/console "all venues summary")
                        (init-all-venues-info %))}))
  (.log js/console (str "STATE " @state-atom))
  (fn []
      ;(.log js/console (str "VENUES-INFO " (session/get :venues-info)))
      (set-title! (str "venue: " (session/get-in [:venue :name])))
      [:div.row
       [:div.col-md-12
        [:div.row
         [:h2 "Infringement Venues"]
         [:p "---"]]
        [:div.row
         [:input {:type "button"
                  :value (if (:editing? @state-atom)
                           (str "edit")
                           (str "locked"))
                  :on-click #(swap! state-atom update-in [:editing?] not)}]
         [:input {:type "button"
                  :value (if (:saved @state-atom) "saved" "save?")
                  :on-click #(save-venue-to-server! (session/get :venue))}]
         [:input {:type "button"
                  :value "new"
                  :on-click #(create-venue-on-server!)}]]
        [:div.row
         [:p "--------------------------"]
         ;[venues-list-widget]
         (if (session/get-in [:current-venue ])
           [logged-in-user-venues-display])]]]))


(defn venues-list-page []
  ; if no current info map, then get it
  (if (nil? (session/get :venues-info))
    (GET "/api/venue/names"
         {:response-format :json
          :keywords? true
          :handler #(do (.log js/console "all venues summary")
                        (init-all-venues-info %))}))
  (.log js/console (str "STATE " @state-atom))
  (fn []
    ;(.log js/console (str "VENUES-INFO " (session/get :venues-info)))
    (set-title! (str "venue: " (session/get-in [:venue :name])))
    [:div.row
     [:div.col-md-12
      [:div.row
       [:h2 "Infringement Venues"]
       [:p "---"]]
      [:div.row
       [:input {:type "button"
                :value (if (:editing? @state-atom)
                         (str "edit")
                         (str "locked"))
                :on-click #(swap! state-atom update-in [:editing?] not)}]
       [:input {:type "button"
                :value "new"
                :on-click #(create-venue-on-server!)}]]
      [:div.row
       [:p ">---<"]
       [venues-list-widget]
       [logged-in-user-venues-list]]]]))
