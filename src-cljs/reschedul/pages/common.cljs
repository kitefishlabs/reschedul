(ns reschedul.pages.common
  (:require [reagent.core :as r]
            ;[reschedul.util :refer []]
            ;[ajax.core :refer [GET POST]
            [reschedul.session :as session]))

; takes a state-atom AND a save function
(defn control-row [state-atom savefun]
  (fn []
    [:div.row
     [:p
      [:span {:display :inline} "EDIT?:  "]
      [:input {:type "checkbox"
               :value (:editing? @state-atom)
               :on-click #(swap! state-atom update-in [:editing?] not)}]
      [:input {:type "button"
               :value (if (:saved? @state-atom) (str "Saved.") (str "*Save?"))
               :on-click #(savefun)}]]]))

; will be used for something real, soon
(defn hints-pane [schema-kws]
  (let [state :nothing]
    (fn [schema-kws]
      [:div.row
       (cond
         (= state :ok) [:p {:style {:backgroundColor "green"}} "."]
         (= state :warn) [:p {:backgroundColor  "yellow"} "."]
         (= state :invalid) [:p {:backgroundColor  "red"} "."]
         :else [:p {:backgroundColor "white"} "-"])])))

; display value as text when not editing
(defn row [label schema-kws]
  (fn []
    [:div.row.user-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (session/get-in schema-kws))]]
     [:div.col-md-2
      [hints-pane schema-kws]]]))

; display yes or no when not editing
(defn row-bool [label schema-kws]
  (fn []
    [:div.row.user-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (if (true? (session/get-in schema-kws)) "yes" "no"))]]
     [:div.col-md-2
      [hints-pane schema-kws]]]))

; editable text entry (single line)
(defn edit-schema-row [label hint schema-kws]
  (.log js/console (str schema-kws))
  (fn []
    [:div.row.user-schema-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:placeholder hint
                                           :type "text"
                                           :class "form-control"
                                           :value (session/get-in schema-kws)
                                           :on-change (fn [e]
                                                        (session/swap! assoc-in schema-kws (-> e .-target .-value))
                                                        (swap! state-atom assoc-in [:saved?] false))}]]
     [:div.col-md-2 [hints-pane schema-kws]]]))

; editable textarea
(defn edit-schema-textarea-row [label hint schema-kws]
  (fn []
    [:div.row.user-schema-textarea-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:placeholder hint
                                           :type "textarea"
                                           :class "form-control"
                                           :maxLength 1000
                                           :rows 3
                                           :value (session/get-in schema-kws)
                                           :on-change (fn [x]
                                                        (session/swap! assoc-in schema-kws (-> x .-target .-value))
                                                        (swap! state-atom assoc-in [:saved?] false))}]]
     [:div.col-md-2 [hints-pane schema-kws]]]))

;"editable" via dropdown
(defn edit-schema-boolean-row [label schema-kws]
  (fn []
    ;(.log js/console (str "esbm: " schema-kws))
    [:div.row.user-schema-boolean-row
     [:div.col-md-9 [:span label]]
     [:div.col-md-1 ^{:key label} [:select
                                   {:placeholder "no"
                                    :multiple false
                                    :value (if (true? (session/get-in schema-kws)) "yes" "no")
                                    :on-change (fn [x]
                                                 (let [val (-> x .-target .-value)]
                                                   ;(.log js/console (str "->bool: " val))
                                                   (session/swap! assoc-in schema-kws (if (= "yes" val) true false))
                                                   (swap! state-atom assoc-in [:saved?] false)))}
                                   [:option {:key false} "no"]
                                   [:option {:key true} "yes"]]]
     [:div.col-md-2 [hints-pane schema-kws]]]))

;; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-dropdown-row [label schema-kws dropdown-list-map]
  (fn []
    [:div.row.user-schema-dropdown-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label}
     [:select
      {:data-placeholder "Choose one..."
       :multiple false
       :value (session/get-in schema-kws)
       :on-change (fn [resp]
                    (let [curr (-> resp .-target .-value)]
                      (session/assoc-in! schema-kws curr)
                      (swap! state-atom assoc-in [:saved?] false)))}
      (for [pair dropdown-list-map]
        [:option {:key (first pair)} (second pair)])]]
     [:div.col-md-2 [hints-pane schema-kws]]]))

; ordinary single-line-of-text row
(defn schema-row [label hint schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if true
        [edit-schema-row label hint schema-kws]
        [row label schema-kws state-atom])]]))

; ordinary multiple-lines-of-text row
(defn schema-textarea-row [label hint schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-textarea-row label hint schema-kws]
        [row label schema-kws state-atom])]]))

; boolean dropdown menu row
(defn schema-boolean-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-boolean-row label schema-kws]
        [row-bool label schema-kws state-atom])]]))

; regulardropdown menu row
(defn schema-dropdown-row [label schema-kws dropdown-list-map state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-dropdown-row label schema-kws dropdown-list-map]
        [row label schema-kws state-atom])]]))
