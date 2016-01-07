(ns reschedul.views)
  ;(:require [hiccup.page :as page]
  ;          [reschedul.roles :refer [any-granted?]]))

;(defn include-page-styles [sources]
;  (map #(page/include-css %) sources))

;(defn show-errors [errors]
;  [:div {:class "alert alert-danger"}
;   [:ul
;    (map #(vec [:li %]) errors)]])

;(defn layout [{title :title content :content req :request :as props}]
;  (page/html5
;    [:head
;     (include-page-styles
;       (concat [ "/css/bootstrap.min.css"
;                "/css/main.css"]))
;     [:title title]]
;    [:body
;     [:div.container
;      [:h1 title]
;      (when-let [errors (:errors props)] (show-errors errors))
;      content]]))

;(defn input [attrs]
;  [:div
;   [:label {:for (:field attrs) :class "control-label"} (:label attrs)]
;   [:input {:type (or (:type attrs) "text")
;            :class "form-control"
;            :id (:field attrs)
;            :name (:field attrs)
;            :placeholder (:label attrs)}]])

;(defn login [req]
;  [:div {:class "row"}
;   [:div {:class "col-sm-9 col-lg-10"} [:p {} "Login to Reschedul..."]]
;   [:div {:class "col-sm-3 col-lg-2"}
;    [:form {:role "form" :method "POST"}
;     [:div {:class "form-group"} (input {:field "username" :label "Username"})]
;     [:div {:class "form-group"} (input {:field "password" :label "Password" :type "password"})]
;     [:div {:class "form-group"} [:button {:type "submit" :class "btn btn-default"} "Login"]]]]])


;(defn account [req]
;  [:div "Showing account info for logged in user here..."])
;(defn accounts [req]
;  [:div "Showing listing of accounts, only visible to store admins"])