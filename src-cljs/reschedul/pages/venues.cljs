(ns reschedul.pages.venues
  (:require [reagent.core :as reagent :refer [atom]]))

(defn venues-page []
  [:div.container
   [:div.jumbotron
    [:h1 "Venues"]
    [:p "Powered by reschedul, a fork of " [:a.btn.btn-primary.btn-md {:href "http://luminusweb.net"} "Luminus»"]]]
   [:div.row
    [:div.col-md-12
     [:h2 "All Venues"]]]])