(ns reschedul.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [reschedul.util :refer [hook-browser-navigation!
                                    ;fetch-venue
                                    text
                                    set-current-venue!
                                    set-page!
                                    maybe-login]]
            [reschedul.pages.home :refer [home-page]]
            [reschedul.pages.about :refer [about-page]]
            [reschedul.pages.users :refer [users-page]]
            [reschedul.pages.venues :refer [venues-page set-current-venue! set-all-venues-info]]
            [reschedul.pages.proposals :refer [proposals-page]])
  (:import goog.History))

(defn header-jumbotron []
  [:div.header
   [:div.jumbotron
    [:h1 "Buffalo Infringement"]
    [:p "11 days of art under the radar"]]])

(defn footer []
  [:div.footer
   [:p (str "Copyright © 2015.") ;(.getFullYear (js/Date.)) " ")

    (when-not (session/get :admin) [:span " (" [:a {:on-click #(secretary/dispatch! "#/login")} #_{:href "#/login"} (text :login)] ")"])
    (text :powered-by)
    [:a {:href "http://github.com/kitefishlabs"} " Kitefish Labs"]]])

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
   :about #'about-page
   :users #'users-page
   :venues #'venues-page
   :proposals #'proposals-page
   :default #'home-page})

(defn page []
  [:div.container
   [header-jumbotron]
   ;(.log js/console (str "pg: " (session/get :page)))
     [(pages (session/get :page))]
   (maybe-login)
   [footer]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :page :home))

(secretary/defroute "/about" []
                    (session/put! :page :about))

(secretary/defroute "/users" []
                    (session/put! :page :users))

(secretary/defroute "/venues" []
                    ;(GET "/api/venues" {:handler #(session/put! :venues %)})
                    (session/put! :page :venues))

(secretary/defroute "/venues/:pg/:per" {:as params}
                    (session/put! :page :venues)
                    (session/put! :offset (:pg params))
                    (session/put! :per (:pr params)))

(secretary/defroute "/proposals" []
                    (session/put! :page :proposals))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []

  ; mobile - sniffing?

  (fetch-docs!)

  (hook-browser-navigation!)

  ; additional GETs
  ; yuggoth fetches here based on the URL
  ;(fetch-venue ID set=venue-and-home-page!)
  (GET "/api/venues/info" {:response-format :json
                           :keywords? true
                           :handler #(do (.log js/console "\n\nWARNING\n-\nSHOULD NEVER SEE THIS POST-STARTUP!!!\n\n")
                                         (set-all-venues-info %))})

  (GET "/api/venue" {:response-format :json
                     :keywords? true
                     :handler #(do (.log js/console "\n\nWARNING\n-\nSHOULD NEVER SEE THIS POST-STARTUP!!!\n\n")
                                (set-current-venue! %))})

  (mount-components))

;(init!)