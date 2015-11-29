(ns reschedul.pages.home
  (:require [reagent.core :as reagent :refer [atom]]))

(defn home-page []
  [:div.container
   [:div.jumbotron
    [:h1 "Buffalo Infringement"]
    [:p "Powered by reschedul, a fork of " [:a.btn.btn-primary.btn-md {:href "http://luminusweb.net"} "Luminus»"]]]
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to the Buffalo Infringement Scheduling App and Website"]]]
   ;(when-let [docs (session/get :docs)]
   ;  [:div.row
   ;   [:div.col-md-12
   ;    [:div {:dangerouslySetInnerHTML
   ;           {:__html (md->html docs)}}]]])
   ])