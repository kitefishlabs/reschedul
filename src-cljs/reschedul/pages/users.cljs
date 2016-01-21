(ns reschedul.pages.users
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]
            [reagent.core :as r]
            [reschedul.util :refer [set-title! empty-all-string-values error-handler trim-list-of-strings]]
            [reschedul.pages.common :refer [hints-pane]]
            ))

(defonce state (r/atom { :editing? false :saved true :admin? false :loaded false}))

; MAYBE?
;(defn set-current-stats []
;  (GET (str "/api/users/stats")
;       {:response-format :json
;        :keywords?       true
;        :handler         #(do (.log js/console (str "set-curr-stats: " %))
;                              (session/put! :stats %))})
;  (js/scroll 0 0))
;
;(defn show-stats []
;  [:div.col-md-12
;   [:div.row
;    [:a ]
;    [:p (str (session/get-in [:stats :unique-users]) " current users.")]
;    [:p (str (session/get-in [:stats :admin-users]) " current admin users.")]
;    [:p (str (session/get-in [:stats :unique-venues]) " current venues.")]
;    [:p (str (session/get-in [:stats :unique-proposals]) " current proposals.")]]])

;(defn row [label schema-kws]
;  (fn []
;    [:div.row.user-row
;     [:div.col-md-4 [:span label]]
;     [:div.col-md-6 ^{:key label} [:span (str (session/get-in schema-kws))]]
;     [:div.col-md-2
;      [hints-pane schema-kws]]]))

(defn artist-row [] ;[username name number-of-proposals]
  [:div.row.artist-row
   [:div.col-md-2 [:span "username"]]
   [:div.col-md-2 [:span "name"]]
   [:div.col-md-2 [:span "num proposals"]]
   [:div.col-md-2 [:span "contact link"]]
   [:div.col-md-2 [:span "n/a"]]
   [:div.col-md-2 [:span "n/a"]]])


(defn users-page []
  (let [user (session/get :user)
        artists (r/atom {})]
    ;(get-all-users-from-server)
    (fn []
      (set-title! "Artists")
      [:div.col-md-12
       [:div.row
        [:h2 "All Artists Dashboard"]
        [:p (str "Wecome back, " (:username user) ", you're an " (:role user) ".")]
        (for [usr artists]
          [artist-row])]])))
