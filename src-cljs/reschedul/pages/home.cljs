(ns reschedul.pages.home
  (:require [reagent.core :as r]
            [reschedul.util :refer [set-title!]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]
            [reschedul.pages.common :refer [control-row
                                            hints-pane
                                            schema-row
                                            schema-textarea-row
                                            schema-boolean-row
                                            schema-checkbox-row
                                            schema-dropdown-row]]))

(defonce state-atom (r/atom {:editing? false :saved? true}))


(defn save-user-to-server []
  (let [user (session/get-in [:user])]
    (.log js/console (str "save-to-server: " user))
    (POST (str "/api/users/" (:_id user))
          {:params user
           :error-handler #(.log js/console (str "save-user-to-server ERROR" %))
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "save-user-to-server success resp: " resp))
                      ;force the send of the user to the server?
                      ; better - add to recents!
                      (session/assoc-in! [:user] resp))})))

(defn get-user-from-server [user]
  (GET (str "/api/users/" (:_id user))
       {:params user
        :error-handler #(.log js/console (str "get-user-from-server ERROR" %))
        :response-format :json
        :keywords? true
        :handler (fn [resp]
                   (.log js/console (str "get-user-from-server success resp: " resp))
                   ;force the send of the user to the server?
                   ; better - add to recents!
                   (session/assoc-in! [:user] resp))}))

(def user-role-choices
  (array-map
    :guest 0
    :user 1       ; create, edit what you own
    :scheduler 2  ; create, edit what you own, edit all proposals
    :organizer 3  ; create, edit what you own, edit all proposals/venues/group shows, assign proposals to schedulers, schedule
    :admin 4))    ; create, edit anything, assign anyone



(defn logged-in-user-data-display []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "User Info")]
      [control-row state-atom]]
     [:div.panel-body
      [:div ;{:class (if (session/get :mobile?) "post-mobile" "post")} ; TODO: take care of this!
       [schema-row "Username" "cannot edit" [:user :username] state-atom]
       [schema-row "Full name" "Please use your full legal name." [:user :full-name] state-atom]
       [schema-dropdown-row "Role" [:user :role] user-role-choices state-atom]]]]))

(defn logged-in-user-contact-data-display []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Contact Info")]
      [control-row state-atom]]
     [:div.panel-body
      [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
       [schema-row "email" [:user :email] state-atom]
       [schema-row "backup email" [:user :backup-email] state-atom]
       [schema-row "cell phone" [:user :cell-phone] state-atom]
       [schema-row "2nd phone" [:user :second-phone] state-atom]
       [schema-dropdown-row "preferred contact method" [:user :preferred-contact-method] state-atom]
       [schema-row "notes" [:user :notes] state-atom]]]]))



(defn home-page []
  (.log js/console (str @session/state))
  (fn []
    (if (nil? (session/get-in [:user]))
      (set-title! "Infringement Festival Website")
      (set-title! "Infringement Festival Dashboard"))
    [:div.row

     ;; header area
     (if (nil? (session/get-in [:user]))

       [:div.col-md-12
        [:div.row
         [:h2 "Infringement Festival Website"]
         [:p "11 days of art, music, dance, film, poetry, theater, and more!"]]
        [:div.row
         [:h3 "WELCOME"]
         [:p ""]
         [:p "A few words and images introducing us and out mission."]
         [:p "Links and instructions."]
         [:p "Etc."]]]

         ;; logged in !!
       [:div.col-md-12
        [:div.row
         [:h2 "Infringement Festival Dashboard"]
         [:p (str "Logged in: " (:username user))]]
        [logged-in-user-data-display]
        [logged-in-user-contact-data-display]
        [:h3 "Proposals"]])]))


;get-logged-in-user-proposals-from-server