(ns reschedul.pages.users
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]
            [reagent.core :as r]
            [reschedul.util :refer [set-title! empty-all-string-values error-handler]]))

(defonce state (r/atom { :editing? false :saved true :admin? false :loaded false }))

(defn set-current-user [venue]
  ;(.log js/console (str "set venue!: " venue))
  (session/put! :user venue)
  ;(set-recent!)
  (js/scroll 0 0))

(defn create-user! [user]
  (let [stripped-user (empty-all-string-values user)
        empty-user (assoc-in stripped-user [:active] true)] ;the hollow man, sew to speak
    (.log js/console (str "empty user" empty-user))
    (POST "/api/user"
          {:params empty-user
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "create-user-to-server success resp: " resp))
                      ;force the send of the venue to the server
                      ; TODO: -> add to recents!
                      (session/assoc-in! [:user] resp)
                      (swap! state update-in [:saved] not))})))

(defn save-user-to-server [user]
  (POST (str "/api/venue/" (str (:_id user)))
        {:params user
         :error-handler #(.log js/console "save-user-to-server ERROR")
         :response-format :json
         :keywords? true
         :handler (fn [resp]
                    (.log js/console (str "save-user-to-server success resp: " resp))
                    ;force the send of the venue to the server?
                    ; better - add to recents!
                    (session/assoc-in! [:user] resp)
                    (swap! state update-in [:saved] not))}))


(defn row [id label]
  (fn []
    [:div.row.venue-row.xs
     [:div.col-md-2 [:span label]]
     [:div.col-md-4 ^{:key label} [:span (str (session/get-in [:user id]))]]
     [:div.col-md-3
      [:p "can't edit"]]
     [:div.col-md-3
      [:button.btn.btn-xs "flag"]
      [:button.btn.btn-xs "comment"]]]))

(defn edit-user-row [id label]
  ;(let [atom (atom {})]
  (fn []
    [:div.row.venue-edit-row
     [:div.col-md-2 [:span label]]
     [:div.col-md-5 ^{:key label} [:input {:type "text"
                                           :class "form-control"
                                           :value (session/get-in [:user id])
                                           :on-change #(do (swap! state assoc-in [:saved] false)
                                                           (session/assoc-in! [:user id] (-> % .-target .-value)))}]]
     [:div.col-md-2
      [:p "status"]]
     [:div.col-md-3
      [:button.btn.btn-xs {:on-click #()} "reset"]
      [:button.btn.btn-xs "+note"]
      [:button.btn.btn-xs "flag"]]]))

(defn user-row [label id]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (:editing? @state)
        [edit-user-row id label]
        [row id label])]]))

(defn users-page []
  (.log js/console (session/get [:user :username]))
  (GET (str "/api/user/" (session/get [:user :username]))
       {:response-format :json
        :keywords?       true
        :handler         #(set-current-user %)})
  (fn []
    (set-title! (str "user: " (session/get-in [:user name])))
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
                :on-click #(save-user-to-server (session/get :venue))}]
       [:input {:type "button"
                :value "new"
                :on-click #(create-user! (session/get :venue))}]
       ;[venues-list-widget]
       ]
      [:div.row
       [:div.col-md-12
        [:h2 "User Info"]
        [:div.row
         [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
          [user-row "username" :username]
          [user-row "first name" :first_name]
          [user-row "last name" :last_name]
          [user-row "email" :email]
          [user-row "phone" :phone]
          [user-row "best contact method" :best_contact]
          [user-row "admin" :admin]]]]]]]))