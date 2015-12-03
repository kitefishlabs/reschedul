(ns reschedul.pages.about
  (:require [reagent.core :as reagent :refer [atom]]))

(defn about-page []
  [:div.row
   [:div.col-sn-12
    [:h2 "What is Infringement?"]
    [:p "The buffalo “infringement” festival is a non-profit-driven, non-hierarchical grassroots endeavor bringing
     together a broad range of eclectic, independent, experimental, and controversial art of all forms. Visual,
     performing, musical, and media arts are all welcome here. Taking place in multiple venues in and around Buffalo’s
     Allentown District, the festival is an annual eleven-day event running from the last weekend of July through the
     first weekend of August. See the Infringement Festival International's mandate and our 2006 Mission Statement to
     learn more."]
    [:h2 "Who is Infringement?"]
    [:ul
     [:li [:a {:href "mailto:dga8787@aol.com?Subject=Infringement%20Help"} "David Adamczyk - Music & Street Performances"]]
     [:li [:a {:href "mailto:deathranch@roadrunner.com?Subject=Infringement%20Help"} "Marty Boratin - Music"]]
     [:li [:a {:href "mailto:visualinfringement@live.com?Subject=Infringement%20Help"} "Amy Duengfelder & Cat Mcarthy - Visual Art"]]
     [:li [:a {:href "mailto:danceundertheradar@Gmail.com?Subject=Infringement%20Help"} "Leslie Fineberg - Dance"]]
     [:li [:a {:href "mailto:pr@infringement.org?Subject=Infringement%20Help"} "Heather Gring - PR, etc."]]
     [:li [:a {:href "mailto:undividedwholness@gmail.com?Subject=Infringement%20Help"} "George Hampton - Housing"]]
     [:li [:p "James Moffit - Graphic Design, Posters, Art"]]
     [:li [:a {:href "mailto:depape@buffalo.edu?Subject=Infringement%20Help"} "Dave Pape - Treasurer / Web Design"]]
     [:li [:a {:href "mailto:b00bflo@gmail.com?Subject=Infringement%20Help"} "Marek Parker - Poetry & Literature"]]
     [:li [:a {:href "mailto:merlinsbooking@Gmail.com?Subject=Infringement%20Help"} "Curt Rodderdam - Music & Fundrasing"]]
     [:li [:p "Bill Smythe - Venue Coordinator"]]
     [:li [:a {:href "mailto:buffaloinfringementfilms@gmail.com?Subject=Infringement%20Help"} "Tom Stoll - Volunteers, Fundraising & Web"]]
     [:li [:p "Dan Zeis - Logo Design (2015)"]]]
    [:p "Plus, of course, the hundreds of performers, and thousands of audience, family, and community that support the arts in Buffalo!"]
    [:h3 "Sponsors"]
    [:p "coming soon..."]
    [:h2 "Direct Monetary Donations"]
    [:p "This festival is run completely on unicorn wishes and happy sunshine rays! Just kidding! There are real
     dollars that have to go into this grassroots festival for things like equipment, technicians, and most importaintly the printed
     schedule."]
    [:p "Here is a (partial) list of our Indie GoGo donors who helped make Infringement possible in 2015:\n\nravi padmanabha, Lisa M Cruz,
     David Rivers, isaiah37@gmail.com, Jonathan M Filbert, Kerry Rubinstein, Roger Paolini, Vanessa R Oswald, Wit Wichaidit, Lynette Seliger,
     Christina E Rausa, michael fanelli, Shea Akers, John R Hastings, Deborah Obarka, michele costa, Andrew Delmonte, mary c uebbing,
     Jenece C Gerber, Robin G Jansma, Dennis J Reed Jr, Janna Willoughby-Lohr, Jennifer Whitmore, curt rotterdam, James A. marzo, Laura Lonski,
     Richard R. Haynes, Dave Pape"]]])
