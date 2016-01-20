(ns reschedul.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]
            [reschedul.session :as session]
            [secretary.core :as secretary :include-macros true]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [reschedul.util :refer [hook-browser-navigation!
                                    error-handler
                                    text

                                    set-page!
                                    set-title!]]
            [reschedul.pages.home :refer [home-page]]
            [reschedul.pages.about :refer [about-page]]
            [reschedul.pages.users :refer [users-page]]
            [reschedul.pages.venues :refer [;venues-page

                                            ;init-all-venues-info
                                            venues-list-page venue-detail-page]]
            [reschedul.pages.proposals :refer [proposals-page]])
  (:import goog.History))


(def login-form-data (atom {:username "" :password ""}))
(def register-form-data (atom {:username "" :email "" :first_name "" :last_name "" :admin false :role :user :password1 "" :password2 ""}))
(def submitting-login? (atom false))
(def submitting-register? (atom false))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn login-button-submit []
  [:div
   (if @submitting-login?
     [:span.loader.pull-right]
     [:span.btn.btn-default.pull-right
      {:on-click #(POST "/api/auth/login" {:response-format :json
                                           :keywords?       true
                                           :params          @login-form-data
                                           :error-handler   error-handler
                                           :handler         (fn [resp]
                                                              (.log js/console "--->")
                                                              (.log js/console resp)
                                                              (let [has-identity? (:user resp)]
                                                                (if has-identity?
                                                                  (do (.log js/console (str "user--> " (:user resp)))
                                                                      (reset! submitting-login? false)
                                                                      (session/put! :page :home)
                                                                      (session/assoc-in! [:user] (:user resp)))
                                                                  (do (.log js/console "NO USER LOGGED IN!!!")
                                                                      ;(.log js/console "SHOULD PROBABLY REDIRECT to /login !!!")
                                                                      (session/put! :page :login)))))})}
      "Login"])])

(defn register-button-submit []
  [:div
   (if @submitting-register?
     [:span.loader.pull-right]
     [:span.btn.btn-default.pull-right
      {:on-click #(POST "/api/auth/register" {:response-format :json
                                              :keywords?       true
                                              :params          @register-form-data
                                              :error-handler   error-handler
                                              :handler         (fn [resp]
                                                                (.log js/console "--->")
                                                                (.log js/console resp)
                                                                (let [has-identity? (:user resp)]
                                                                  (if has-identity?
                                                                    (do (.log js/console (str "user--> " (:user resp)))
                                                                        (reset! submitting-register? false)
                                                                        (session/put! :page :login)
                                                                          (session/assoc-in! [:user] (:user resp)))
                                                                    (do (.log js/console "NO USER REGISTERED!!!")
                                                                        ;(.log js/console "SHOULD PROBABLY REDIRECT to /login !!!")
                                                                        (session/put! :page :register)))))})}
      "Register"])])

(defn login-template []
  [:div.col-md-12
   [:div.row
    [:p "Login to Reschedul..."]]
   [:form
    (row "username" [:input.form-control {:type :text
                                          :id :username
                                          :value (:username @login-form-data)
                                          :on-change #(swap! login-form-data assoc :username (-> % .-target .-value))}])
    (row "password" [:input.form-control {:type :password
                                          :id :password
                                          :value (:password @login-form-data)
                                          :on-change #(swap! login-form-data assoc :password (-> % .-target .-value))}])
    [:div.form-group [login-button-submit]]]])


(defn register-template []
  [:div.col-md-12
   [:div.row
    [:p "Register for Reschedul..."]]
   [:form
    (row "username" [:input.form-control {:type :numeric
                                          :id :username
                                          :value (:username @register-form-data)
                                          :on-change #(swap! register-form-data assoc :username (-> % .-target .-value))}])
    (row "email" [:input.form-control {:type :text
                                       :id :email
                                       :value (:email @register-form-data)
                                       :on-change #(swap! register-form-data assoc :email (-> % .-target .-value))}])
    (row "first name" [:input.form-control {:type :text
                                       :id :first_name
                                       :value (:first_name @register-form-data)
                                       :on-change #(swap! register-form-data assoc :first_name (-> % .-target .-value))}])
    (row "last name" [:input.form-control {:type :text
                                       :id :last_name
                                       :value (:last_name @register-form-data)
                                       :on-change #(swap! register-form-data assoc :last_name (-> % .-target .-value))}])
    (row "password" [:input.form-control {:type :password
                                          :id :password1
                                          :value (:password1 @register-form-data)
                                          :on-change #(swap! register-form-data assoc :password1 (-> % .-target .-value))}])
    (row "password again" [:input.form-control {:type :password
                                                :id :password2
                                                :value (:password2 @register-form-data)
                                                :on-change #(swap! register-form-data assoc :password2 (-> % .-target .-value))}])

    [:div.form-group [register-button-submit]]]])

(defn login-page []
  (let [logged-in-as (session/get-in [:user])
        formdoc (atom {:username "username"
                       :password "password"})]
    (reset! submitting-login? false)
    (fn []
      (if (nil? logged-in-as)
        (set-title! (str "Login, please... "))
        (set-title! (str "Logged in as " logged-in-as)))
      [:div.col-md-12
       [:div.row
        [bind-fields
         login-template
         formdoc]]])))

(defn register-page []
  (let [logged-in-as (session/get-in [:user])
        formdoc (atom {:username ""
                       :email ""
                       :password1 ""
                       :password2 ""})]
    (reset! submitting-register? false)
    (fn []
      (if (nil? logged-in-as)
        (set-title! (str "Register, please... "))
        (set-title! (str "Logged in as " logged-in-as)))
      [:div.col-md-12
       [:div.row
        [bind-fields
         register-template
         formdoc]]])))

(defn header-page-header []
  [:div.page-header
   [:h1 "Buffalo Infringement"]
   [:p "11 days of art under the radar"]])

(defn header-jumbotron []
  [:div.header
   [:div.jumbotron
    [:h1 "Buffalo Infringement"]
    [:p "11 days of art under the radar"]]])

(defn footer []
  [:div.footer
   [:p (str "Copyright © 2015.  ") ;(.getFullYear (js/Date.)) " ")
    " - Powered by: " [:a {:href "http://github.com/kitefishlabs"} " Kitefish Labs"]]])

(defn nav-link [uri title page collapsed?]
  [:li {:class (when (= page (session/get :page)) "active")}
   [:a {:href uri
        :on-click #(reset! collapsed? true)}
    title]])

(defn navbar []
  (let [collapsed? (atom true)]
    (fn []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:button.navbar-toggle
          {:class         (when-not @collapsed? "collapsed")
           :data-toggle   "collapse"
           :aria-expanded @collapsed?
           :aria-controls "navbar"
           :on-click      #(swap! collapsed? not)}
          [:span.sr-only "Toggle Navigation"]
          [:span.icon-bar]
          [:span.icon-bar]
          [:span.icon-bar]]
         [:a.navbar-brand {:href "#/"} "reschedul"]]
        [:div.navbar-collapse.collapse
         (when-not @collapsed? {:class "in"})
         [:ul.nav.navbar-nav
          [nav-link "#/" "Home" :home collapsed?]
          [nav-link "#/about" "About" :about collapsed?]
          [nav-link "#/users" "Users" :users collapsed?]
          [nav-link "#/venues" "Venues" :venues collapsed?]
          [nav-link "#/proposals" "Proposals" :proposals collapsed?]
          (if (session/get :user)
            [nav-link "#/logout" "Logout" :logout collapsed?]
            [nav-link "#/login" "Login" :login collapsed?])
          (if (nil? (session/get :user))
            [nav-link "#/register" "Register" :register collapsed?])]]]])))


(def pages
  {:home #'home-page
   :login #'login-page
   :register #'register-page
   :about #'about-page
   :users #'users-page
   :venues-list #'venues-list-page
   :venue-detail #'venue-detail-page
   :proposals #'proposals-page
   :default #'home-page})

(defn page []
  [:div.container
   [header-page-header]
   [(pages (session/get :page))]
   [footer]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (.log js/console "base route")
                    (session/put! :page :home))

(secretary/defroute "/about" []
                    (.log js/console "about route")
                    (session/put! :page :about))

(secretary/defroute "/users" []
                    (.log js/console "users route")
                    (session/put! :page :users))

(secretary/defroute "/venues" []
                    (.log js/console "venues route")
                    (session/put! :page :venues-list))

(secretary/defroute "/venues/:_id" [_id]
                    (.log js/console (str "venues route: " _id))

                    (session/put! :page :venue-detail))

(secretary/defroute "/proposals" []
                    (.log js/console "proposals route")
                    (session/put! :page :proposals))

(secretary/defroute "/login" []
                    (.log js/console "login route")
                    (session/put! :page :login))

(secretary/defroute "/register" []
                    (.log js/console "register route")
                    (session/put! :page :register))

;; -------------------------
;; Initialize app
;(defn fetch-docs! []
;  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []

  ; mobile - sniffing?

  ;(fetch-docs!)

  (hook-browser-navigation!)

  ; additional GETs
  ; yuggoth fetches here based on the URL
  ;(fetch-venue ID set=venue-and-home-page!)

  ;(session/reset! {:user (session/get :user "guest")} )
  (session/reset! {:page :home})
  (mount-components))

;(init!)