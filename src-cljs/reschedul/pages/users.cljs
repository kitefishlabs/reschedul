(ns reschedul.pages.users
  (:require [reagent.core :as reagent :refer [atom]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]
            [reagent.core :as r]
            [reschedul.util :refer [set-title! empty-all-string-values error-handler trim-list-of-strings]]))

(defonce state (r/atom { :editing? false :saved true :admin? false :loaded false}))

(defn set-current-stats []
  (GET (str "/api/users/stats")
       {:response-format :json
        :keywords?       true
        :handler         #(do (.log js/console (str "set-curr-stats: " %))
                              (session/put! :stats %))})
  (js/scroll 0 0))

; TODO: make this a general get?
;(defn get-current-user [id]
;  (GET (str "/api/users/" id)
;       {:response-format :json
;        :keywords? true
;        :error error-handler
;        :handler #(do (.log js/console "---> get current user")
;                      (set-current-user! %))}))

(defn show-stats []
  [:div.col-md-12
   [:div.row
    [:a ]
    [:p (str (session/get-in [:stats :unique-users]) " current users.")]
    [:p (str (session/get-in [:stats :admin-users]) " current admin users.")]
    [:p (str (session/get-in [:stats :unique-venues]) " current venues.")]
    [:p (str (session/get-in [:stats :unique-proposals]) " current proposals.")]]])

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
                      ;force the send of the user to the server
                      ; TODO: -> add to recents!
                      (session/assoc-in! [:user] resp)
                      (swap! state update-in [:saved] not))})))

(defn save-user-to-server [user]
  (POST (str "/api/users/" (str (:_id user)))
        {:params user
         :error-handler #(.log js/console "save-user-to-server ERROR")
         :response-format :json
         :keywords? true
         :handler (fn [resp]
                    (.log js/console (str "save-user-to-server success resp: " resp))
                    ;force the send of the user to the server?
                    ; better - add to recents!
                    (session/assoc-in! [:user] resp)
                    (swap! state update-in [:saved] not))}))


(defn row [id label]
  (fn []
    [:div.row.user-row.xs
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
    [:div.row.user-edit-row
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

;(defn users-did-mount []
;  (let [names (->> (session/get :users-names-map)
;                   keys
;                   (map name)
;                   (trim-list-of-strings))]
;    (js/$ (fn []
;            (.autocomplete (js/$ "#usernames")
;                           (clj->js {:source names}))))))

;(defn users-list-widget []
;  (fn []
;    (.log js/console "setup users list ----------->")
;    [(reschedul.util/mounted-component
;       [:div.ui-widget
;        [:label {:for "tags"} "Choose a user: "]
;        [:input#usernames]
;        [:input {:type "button"
;                 :value "load"
;                 :on-click (fn []
;                             (let [user-key (-> (.getElementById js/document "usernames") .-value keyword)
;                                   id (get-in (session/get :users-names-map) [user-key :_id])]
;                               (.log js/console id)
;                               (get-current-user id)
;                               (.log js/console (str "get-current" (session/get-in [:user])))
;                               ))}]]
;       #(users-did-mount))]))

(defn users-page []
  (let [user (session/get :user "NOBODY")]
    (.log js/console user)
    (fn []
      (set-title! (str "Dashboard: " (:username user)))
      [:div.col-md-12
       [:div.row
        [:h2 "Dashboard"]
        [:p (str "Wecome back, " (:username user))]]])))

         ; [:div.col-md-12
         ;  [:div.row
         ;   [:p (str "Logged in as: " (session/get-in [:user :username]))]]
         ;  [:div.row
         ;   [:input {:type "button"
         ;            :value (if (:editing? @state)
         ;                     (str "edit")
         ;                     (str "locked"))
         ;            :on-click #(swap! state update-in [:editing?] not)}]
         ;
         ;   [:input {:type "button"
         ;            :value "new"
         ;            :on-click #(create-user! (session/get :user))}]
         ;   [:input {:type "button"
         ;            :value (if (:saved @state) "saved" "save?")
         ;            :on-click #(save-user-to-server (session/get :user))}]
         ;   [:input {:type "button"
         ;            :value (if (:saved @state) "saved" "save?")
         ;            :on-click #(save-user-to-server (session/get :user))}]]
         ;  ;[users-list-widget]
         ;  [:div.row
         ;   [:h2 "User Info"]
         ;   [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
         ;    [user-row "username" :username]
         ;    [user-row "first name" :first_name]
         ;    [user-row "last name" :last_name]
         ;    [user-row "email" :email]
         ;    [user-row "phone" :phone]
         ;    [user-row "best contact method" :best_contact]
         ;[user-row "admin" :admin]]]]
        ;]])))