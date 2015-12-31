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
                                    set-current-venue!
                                    set-page!
                                    set-title!]]
            [reschedul.pages.home :refer [home-page]]
            [reschedul.pages.about :refer [about-page]]
            [reschedul.pages.users :refer [users-page]]
            [reschedul.pages.venues :refer [venues-page set-current-venue! init-all-venues-info]]
            [reschedul.pages.proposals :refer [proposals-page]])
  (:import goog.History))


(def login-form-data (atom {:username "admin" :password "password1"}))
(def submitting  (atom false))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn submit-button []
  [:div
   (if @submitting
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
                                                                      (session/put! :page :home)
                                                                      (session/assoc-in! [:user] (:user resp)))
                                                                  (do (.log js/console "NO USER LOGGED IN!!!")
                                                                      (.log js/console "SHOULD PROBABLY REDIRECT to /login !!!")))))})}

      "Login"])])

(defn login-template []
  [:div.col-md-12
   [:div.row
    [:p "Login to Reschedul..."]]
   [:form
    (row "username" [:input.form-control {:field :text
                                          :id :username
                                          :value (:username @login-form-data)
                                          :on-change #(swap! login-form-data assoc :username (-> % .-target .-value))}])
    (row "password" [:input.form-control {:field :password
                                          :id :password
                                          :value (:password @login-form-data)
                                          :on-change #(swap! login-form-data assoc :password (-> % .-target .-value))}])
    [:div.form-group [submit-button]]]])


(defn login-page []
  (let [logged-in-as (session/get-in [:user])
        formdoc (atom {:username "username"
                       :password "password"})]
    (.log js/console (str "logged-in-as: " logged-in-as))
    (fn []
      (if (nil? logged-in-as)
        (set-title! (str "Login, please... "))
        (set-title! (str "Logged in as " logged-in-as)))
      [:div.col-md-12
       [:div.row
        [bind-fields
         login-template
         formdoc
         (fn [id value map]
           (do
             (.log js/console "submitting")))
         ;    (.log js/console (str "id" id))
         ;    (.log js/console (str "value" value))
         ;    (.log js/console (str "map" map))
         ;    (reset! submitting false)))
         ]]])))



(defn header-jumbotron []
  [:div.header
   [:div.jumbotron
    [:h1 "Buffalo Infringement"]
    [:p "11 days of art under the radar"]]])

(defn footer []
  [:div.footer
   [:p (str "Copyright © 2015.") ;(.getFullYear (js/Date.)) " ")
    (when-not (session/get :login)
      [:span " (" [:a {:on-click #(secretary/dispatch! "#/login") :href "#/login"} "login"] ")"])
    (text :powered-by) [:a {:href "http://github.com/kitefishlabs"} " Kitefish Labs"]]])

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
          (if (session/get :login)
            [nav-link "#/logout" "Logout" :logout collapsed?]
            [nav-link "#/login" "Login" :login collapsed?])]]]])))


(def pages
  {:home #'home-page
   :login #'login-page
   :about #'about-page
   :users #'users-page
   :venues #'venues-page
   :proposals #'proposals-page
   :default #'home-page})

(defn page []
  [:div.container
   [header-jumbotron]
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
                    (session/put! :page :venues))

(secretary/defroute "/proposals" []
                    (.log js/console "proposals route")
                    (session/put! :page :proposals))

(secretary/defroute "/login" []
                    (.log js/console "login route")
                    (session/put! :page :login))

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