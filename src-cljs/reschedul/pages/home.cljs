(ns reschedul.pages.home
  (:require [reagent.core :as r]
            [reschedul.util :refer [set-title!]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]))

(defonce state-atom (r/atom {:editing? false :saved? true :loaded? false}))


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
                      (session/assoc-in! [:user] resp)
                      ;(swap! state-atom assoc-in [:saved] true)
                      )})))

(defn get-user-from-server []
  (let [user (session/get-in [:user])]
    (.log js/console (str "get-from-server: " user))
    (GET (str "/api/users/" (:_id user))
          {:params user
           :error-handler #(.log js/console (str "get-user-from-server ERROR" %))
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "get-user-from-server success resp: " resp))
                      ;force the send of the user to the server?
                      ; better - add to recents!
                      (session/assoc-in! [:user] resp)
                      ;(swap! state-atom assoc-in [:saved] true)
                      )})))



(defn control-row [kw state-atom]
  (fn []
    [:div.row
     [:input {:type "button"
              :value (if (:editing? @state-atom) (str "Disable edits.") (str "Enable edits."))
              :on-click #(swap! state-atom update-in [:editing?] not)}]
     [:input {:type "button"
              :value (if (:saved? @state-atom) (str "Saved.") (str "*Save data."))
              :on-click #(save-user-to-server)}]
     [:input {:type "button"
              :value "Refresh."
              :on-click #(get-user-from-server)}]]))

(defn group-state-icons [state]
  (fn [state]
    [:div.row
     (cond
      (= state :ok) [:p {:style {:color "green"}} "O"]
      (= state :warn) [:p {:color "yellow"} "*"]
      (= state :invalid) [:p {:color "red"} "X"]
      :else [:p "-"])])) ;:span {:style {:color "black"}}

(defn row [label schema-kws]
  (fn []
    [:div.row.user-row
     [:div.col-md-3 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (session/get-in schema-kws))]]
     [:div.col-md-3
      [group-state-icons :warn]]]))

  ; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-row [label schema-kws]
  (fn []
    [:div.row.user-schema-row
     [:div.col-md-3 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:type "text"
                                           :class "form-control"
                                           :value (session/get-in schema-kws)
                                           :on-change (fn [e]
                                                        (session/swap! assoc-in schema-kws (-> e .-target .-value)))}]]]))


; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-textarea-row [label schema-kws]
  (fn []
    [:div.row.user-schema-textarea-row
     [:div.col-md-3 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:type "textarea"
                                           :class "form-control"
                                           :value (session/get-in schema-kws)
                                           :on-change #(session/swap! assoc-in schema-kws (-> % .-target .-value))}]]]))


(defn schema-row [label schema-kws state-atom]
  ;(let [{:keys [editing?] :as state} @state-atom]
    (fn []
      [:div.row
       [:div.col-md-12
        (if (get-in @state-atom [:editing?])
          [edit-schema-row label schema-kws]
          [row label schema-kws state-atom])]]))

(defn schema-textarea-row [label schema-kws state-atom]
  ;(let [{:keys [editing?] :as state} @state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-textarea-row label schema-kws]
        [row label schema-kws state-atom])]]))


(defn logged-in-user-data-display []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "User Info")]
      [control-row :user state-atom]
      [group-state-icons]]
     [:div.panel-body
      [:div ;{:class (if (session/get :mobile?) "post-mobile" "post")} ; TODO: take care of this!
       [schema-row "username" [:user :username] state-atom]
       [schema-row "first name" [:user :first_name] state-atom]
       [schema-row "last name" [:user :last_name] state-atom]
       [schema-row "role" [:user :role] state-atom]
       [schema-row "admin" [:user :admin] state-atom]
       [schema-textarea-row "notes" [:user :notes] state-atom]]]]))

(defn logged-in-user-contact-data-display []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Contact Info")]
      [control-row :user state-atom]
      [group-state-icons]]
     [:div.panel-body
      [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
       [schema-row "email" [:user :contact-info :email] state-atom]
       [schema-row "backup email" [:user :contact-info :backup-email] state-atom]
       [schema-row "cell phone" [:user :contact-info :cell-phone] state-atom]
       [schema-row "2nd phone" [:user :contact-info :second-phone] state-atom]
       [schema-row "preferred contact method" [:user :contact-info :preferred_contact_method] state-atom]
       [schema-row "notes" [:user :contact-info :notes] state-atom]]]]))

(defn logged-in-user-social-data-display []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Social Info")]
      [control-row :user state-atom]
      [group-state-icons]]
     [:div.panel-body
      [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
       [schema-row "facebook" [:social-info :facebook] state-atom]
       [schema-row "twitter" [:social-info :twitter] state-atom]
       [schema-row "website" [:social-info :website] state-atom]
       [schema-row "soundcloud" [:social-info :soundcloud] state-atom]
       [schema-row "vimeo" [:social-info :vimeo] state-atom]
       [schema-row "youtube" [:social-info :youtube] state-atom]
       [schema-row "mailing-list" [:social-info :vimeo] state-atom]
       [schema-row "notes" [:social-info :notes] state-atom]]]]))

(defn logged-in-user-availability []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Availability Info")]
      [control-row :user state-atom]
      [group-state-icons]]
     [:div.panel-body
      [:div{:class (if (session/get :mobile?) "post-mobile" "post")}
       [schema-row "Wednesday0" [:availability :wednesday0] state-atom]
       [schema-row "Thursday1" [:availability :thursday1] state-atom]
       [schema-row "Friday1" [:availability :friday1] state-atom]
       [schema-row "Saturday1" [:availability :saturday1] state-atom]
       [schema-row "Sunday1" [:availability :sunday1] state-atom]
       [schema-row "Monday1" [:availability :monday1] state-atom]
       [schema-row "Tuesday1" [:availability :tuesday1] state-atom]
       [schema-row "Wednesday1" [:availability :wednesday1] state-atom]
       [schema-row "Thursday2" [:availability :thursday2] state-atom]
       [schema-row "Friday2" [:availability :friday2] state-atom]
       [schema-row "Saturday2" [:availability :saturday2] state-atom]
       [schema-row "Sunday2" [:availability :sunday2] state-atom]
       [schema-row "Monday2" [:availability :monday2] state-atom]]]]))

(defn home-page []
  (fn []
    (let [user (session/get :user "NOBODY")]
      (set-title! "HOME")
      [:div.row
       [:div.col-md-12
        [:div.row
         [:h2 "Dashboard"]
         [:p (str "Logged in: " (:username user))]]
        [:div.row
         [:div.col-md-8
          [logged-in-user-data-display]
          [logged-in-user-contact-data-display]
          [logged-in-user-social-data-display]]
         [:div.col-md-4
          [:p "proposals"]
          [logged-in-user-availability]
          [:p "mentions"]]]]])))
