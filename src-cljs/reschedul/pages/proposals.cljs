(ns reschedul.pages.proposals
  (:require [reagent.core :as r]
            [reschedul.util :refer [set-title!
                                    empty-all-string-values
                                    error-handler]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]))

(defonce state-atom (r/atom {:editing? false :saved? true :loaded? false}))

(defn create-proposal-on-server! []
  (let [empty-proposal {:_id "0" :title "TITLE" :genre "none" :proposer (session/get-in [:user :username]) :state "created"}]
    (.log js/console (str empty-proposal)
    (POST "/api/proposals"
          {:params empty-proposal
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "create-to-server success resp: " resp))
                      ;force the send of the venue to the server
                      ; TODO: -> add to recents!
                      (session/assoc-in! [:current-proposal] resp)
                      ;(swap! state update-in [:saved] not)
                      )}))))

(defn save-proposal-to-server []
  (let [current (session/get-in [:current-proposal])]
    (.log js/console (str "save-proposal-to-server: " current))
    (POST (str "/api/proposals/" (:_id current))
          {:params current
           :error-handler #(.log js/console (str "save-proposal-to-server ERROR" %))
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "save-proposal-to-server success resp: " resp))
                      ;force the send of the user to the server?
                      ; better - add to recents!
                      (session/assoc-in! [:proposals] resp)
                      (session/assoc-in! [:current-proposal] (filter (fn [x] (= (:_id x) (session/get-in [:current-proposal :_id]))) (:proposals resp)))
                      ;(swap! state-atom assoc-in [:saved] true)
                      )})))

(defn get-logged-in-user-proposals-from-server []
  (let [user (session/get-in [:user])
        username (:username user)]
    (.log js/console (str "get-logged-in-user-proposal-from-server: /api/proposals/user/" username))
    (GET (str "/api/proposals/user/" username)
         {:body {:username username}
          :error-handler #(.log js/console (str "get-logged-in-proposal-from-server ERROR" %))
          :response-format :json
          :keywords? true
          :handler (fn [resp]
                     (.log js/console (str "get-logged-in-proposals-from-server success resp: " resp))
                     ;force the send of the user to the server?
                     ; better - add to recents!
                     (session/assoc-in! [:proposals] resp)
                     (session/assoc-in! [:current-proposal] (first resp)))})))
                    ;(swap! state-atom assoc-in [:saved] true)

(defn get-proposal-from-server []
  (let [proposals (session/get-in [:proposals])
        first-proposal (first proposals)]
    (.log js/console (str "get-proposal-from-server: " first-proposal))
    (GET (str "/api/proposals/" (:_id first-proposal))
         {
          :error-handler #(.log js/console (str "get-proposal-from-server ERROR" %))
          :response-format :json
          :keywords? true
          :handler (fn [resp]
                     (.log js/console (str "get-proposal-from-server success resp: " resp))
                     ;force the send of the user to the server?
                     ; better - add to recents!
                     (session/assoc-in! [:proposals] resp)
                     (session/assoc-in! [:current-proposal] (filter (fn [x] (= (:_id x) (session/get-in [:current-proposal :_id]))) (:proposals resp)))
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
              :on-click #(save-proposal-to-server)}]
     [:input {:type "button"
              :value "Refresh."
              :on-click #(get-proposal-from-server)}]]))

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
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (session/get-in schema-kws))]]
     [:div.col-md-2
      [group-state-icons :warn]]]))

; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-row [label schema-kws]
  (fn []
    [:div.row.user-schema-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:type "text"
                                           :class "form-control"
                                           :value (session/get-in schema-kws)
                                           :on-change (fn [e] (session/swap! assoc-in schema-kws (-> e .-target .-value)))}]]
     [:div.col-md-2 [group-state-icons :ok]]]))

(defn edit-schema-boolean-row [label schema-kws]
  (fn []
    [:div.row.user-schema-boolean-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:select
                                   {:data-placeholder "Make a choice"
                                    :multiple false
                                    :value (session/get-in schema-kws)
                                    :on-change #(session/swap! assoc-in schema-kws (-> % .-target .-value))}
                                   [:option {:key false} "no"]
                                   [:option {:key true} "yes"]]]
     [:div.col-md-2 [group-state-icons :ok]]]))


; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-textarea-row [label schema-kws]
  (fn []
    [:div.row.user-schema-textarea-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:type "textarea"
                                           :class "form-control"
                                           :maxlength 1000
                                           :rows 3
                                           :value (session/get-in schema-kws)
                                           :on-change #(session/swap! assoc-in schema-kws (-> % .-target .-value))}]]
     [:div.col-md-2 [group-state-icons :ok]]]))

; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-dropdown-row [label schema-kws]
  (fn []
    [:div.row.user-schema-dropdown-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-8 ^{:key label}
      [:select
       {:data-placeholder "Choose a genre"
        :multiple false
        :value (session/get-in schema-kws)
        :on-change #(session/assoc-in! schema-kws (-> % .-target .-value))}
       (for [pair (array-map
                    :music "music"
                    :dance "dance"
                    :film "film"
                    :spokenword "spokenword"
                    :visualart "visualart"
                    :theater "theater"
                    :none "none")]
         [:option {:key (first pair)} (second pair)])]]]))


(defn schema-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-row label schema-kws]
        [row label schema-kws state-atom])]]))

(defn schema-boolean-row [label schema-kws state-atom]
  ;(let [{:keys [editing?] :as state} @state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-boolean-row label schema-kws]
        [row label schema-kws state-atom])]]))

(defn schema-textarea-row [label schema-kws state-atom]
  ;(let [{:keys [editing?] :as state} @state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-textarea-row label schema-kws]
        [row label schema-kws state-atom])]]))

(defn schema-dropdown-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-dropdown-row label schema-kws]
        [row label schema-kws state-atom])]]))

(defn copy-user-to-primary-contact []
  (let [user (session/get-in [:user])]
    (do
      (session/assoc-in! [:current-proposal :primary-contact-name] (:username user))
      (session/assoc-in! [:current-proposal :primary-contact-email] (get-in user [:contact-info :email]))
      (session/assoc-in! [:current-proposal :primary-contact-phone] (:cell-phone user)))))

; TODO: this needs to be wrapped by auth
(defn logged-in-user-proposals-display []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Proposals for: " (session/get-in [:user :username]))]
      [control-row :user state-atom]
      [group-state-icons]]
     [:div.panel-body
      [:div.col-md-12
       [:input {:name "create"
                :type "button"
                :value "create"
                :on-click #(create-proposal-on-server!)}]
       (for [proposal (session/get-in [:proposals])]
         [:div.row
          ^{:key (:_id proposal)}
          [:input {:type "button"
                   :value (:title proposal)
                   :on-click #(session/assoc-in! [:current-proposal] proposal)}]])
       [schema-row "Proposal Title" [:current-proposal :title] state-atom]
       [schema-dropdown-row "Genre" [:current-proposal :genre] state-atom]
       [schema-dropdown-row "Assigned Genre" [:current-proposal :assigned-genre] state-atom]
       [schema-row "Genre tags" [:current-proposal :genre-tags] state-atom]
       [schema-row "Proposer" [:current-proposal :proposer] state-atom]
       [schema-row "Assigned organizer(s)" [:current-proposal :assigned-organizers] state-atom]
       [:button.btn.btn-xs.btn-primary {:type "button"
                                        :on-click #(copy-user-to-primary-contact)} "copy logged-in user to primary contact"]
       [schema-row "Primary contact name" [:current-proposal :primary-contact-name] state-atom]
       [schema-row "Primary contact email" [:current-proposal :primary-contact-email] state-atom]
       [schema-row "Primary contact phone" [:current-proposal :primary-contact-phone] state-atom]
       [schema-row "Primary contact relationship" [:current-proposal :primary-contact-relationship] state-atom]

       [schema-row "Secondary contact name" [:current-proposal :secondary-contact-name] state-atom]
       [schema-row "Secondary contact email" [:current-proposal :secondary-contact-email] state-atom]
       [schema-row "Primary contact phone" [:current-proposal :primary-contact-phone] state-atom]
       [schema-row "Primary contact relationship" [:current-proposal :primary-contact-relationship] state-atom]
       ; AVAILABILITY@@@

       [schema-row "Number of performers" [:current-proposal :number-of-performers] state-atom]
       [schema-textarea-row "Performers' names" [:current-proposal :performers-names] state-atom]
       [schema-row "Potential conflicts" [:current-proposal :potential-conflicts] state-atom]

       [schema-textarea-row "Description (for organizers)" [:current-proposal :description-private] state-atom]
       [schema-textarea-row "Description (for publicity)" [:current-proposal :description-public] state-atom]
       [schema-textarea-row "Description (140 chars, for newspaper schedule)" [:current-proposal :description-public-140] state-atom]
       [schema-textarea-row "General notes" [:current-proposal :general-notes] state-atom]]]]))

;:availability
;:promotional-info



;(defn performance-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for performances...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;       [schema-row "Setup/teardown time?" [:proposal :performance :setup-time] state-atom]
;       [schema-row "Ideal performance duration?" [:proposal :performance :run-time] state-atom]
;       [schema-row "Teardown time?" [:proposal :performance :teardown-time] state-atom]
;       [schema-row "Approximate rating?" [:proposal :performance :rating] state-atom]
;       [schema-boolean-row "Are all performers 21+?" [:proposal :performance :twentyone?] state-atom]
;       [schema-boolean-row "Does your act require seating?" [:proposal :performance :seating?] state-atom]
;       [schema-boolean-row "Do you project images?" [:proposal :performance :projection-self?] state-atom]
;       [schema-boolean-row "Can other artists project images during your performance?" [:proposal :performance :projection-other?] state-atom]]]]))
;
;
;(defn music-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for music...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;       [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
;       [schema-textarea-row "Describe your power needs/equipment." [:proposal :power-needs] state-atom]
;       [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :amp-needs] state-atom]
;       [schema-textarea-row "Can you provide drums and/or backline amps to a group show?" [:proposal :drums-backline-to-provide] state-atom]
;       [schema-boolean-row "Does your act require a full sound system?" [:proposal :full-sound-system?] state-atom]
;       [schema-textarea-row "Do you have other equipment you are willing to share?" [:proposal :gear-to-share] state-atom]
;       [schema-row "How loud are you? (1-10)" [:proposal :how-loud] state-atom]
;       [schema-row "Anything we should know about your setup?" [:proposal :setup-notes] state-atom]
;       [schema-row "Anything we should know about your tech?" [:proposal :tech-notes] state-atom]]]]))
;
;
;(defn dance-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for dance proposals...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;       [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
;       [schema-textarea-row "Describe your power needs/equipment." [:proposal :power-needs] state-atom]
;       [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :amp-needs] state-atom]
;       [schema-boolean-row "Does your act require a sound system?" [:proposal :basic-sound-system?] state-atom]
;       [schema-boolean-row "Does your act require seating?" [:proposal :seating-needed?] state-atom]
;       [schema-textarea-row "Can other performers share your gear? If so, what?" [:proposal :gear-to-share] state-atom]]]]))
;
;(defn spokenword-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for spoken word and poetry proposals...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;
;       [schema-textarea-row "Describe your space needs." [:proposal :tech :space-needs] state-atom]
;       [schema-textarea-row "Describe your power needs/equipment." [:proposal :tech :power-needs] state-atom]
;       [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :tech :amp-needs] state-atom]
;       [schema-boolean-row "Does your act require a sound system?" [:proposal :tech :basic-sound-system?] state-atom]
;       [schema-boolean-row "Does your act require seating?" [:proposal :tech :seating-needed?] state-atom]
;       [schema-textarea-row "Do you have other equipment you are willing to share?" [:proposal :gear-to-share] state-atom]]]]))
;
;(defn film-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for film proposals...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;       [schema-boolean-row "Live Performance?" [:proposal :live-performance?] state-atom]
;       [schema-boolean-row "Installation?" [:proposal :installation?] state-atom]
;       [schema-row "Genre" [:proposal :film-genre] state-atom]
;       [schema-row "Duration" [:proposal :film-duration] state-atom]
;       [schema-textarea-row "Preview urls + instructions." [:proposal :preview-urls] state-atom]
;       [schema-textarea-row "Can you provide a viewing space? If so, please describe." [:proposal :can-facilitate-screening] state-atom]
;       [schema-textarea-row "Can you provide a projector and/or screen?" [:proposal :can-provide-projector] state-atom]]]]))
;
;
;(defn theater-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for theater proposals...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;       [schema-textarea-row "Do you have a space prearranged? If so, please describe." [:proposal :space-prearranged] state-atom]
;       [schema-boolean-row "Can other performers share your space?" [:proposal :share-space?] state-atom]
;       [:p "For non-traditional (and possibly traditional) spaces..."]
;       [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
;       [schema-textarea-row "Describe your power needs/equipment." [:proposal :power-needs] state-atom]
;       [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :amp-needs] state-atom]
;       [schema-boolean-row "Does your act require a basic sound system (for cd/mp3/laptop)?" [:proposal :basic-sound-system?] state-atom]
;       [schema-boolean-row "Does your act require seating?" [:proposal :seating-needed?] state-atom]
;       [schema-textarea-row "Can other performers share your gear? If so, what?" [:proposal :gear-to-share] state-atom]]]]))
;
;(defn visualarts-proposal-questions []
;  (fn []
;    [:div.panel.panel-default
;     [:div.panel-heading
;      [:h4 (str "Additional questions for visual arts proposals...")]
;      [control-row :current-proposal state-atom]
;      ;[group-state-icons]
;      ]
;     [:div.panel-body
;      [:div.col-md-12
;       [schema-boolean-row "Live Performance?" [:proposal :live-performance?] state-atom]
;       [schema-boolean-row "Installation?" [:proposal :installation?] state-atom]
;       [schema-row "Number of pieces?" [:proposal :number-of-pieces] state-atom]
;       [schema-textarea-row "List pieces and sizes." [:proposal :pieces-list] state-atom]
;       [schema-textarea-row "Do you have gallery space prearranged? If so, what space?" [:proposal :prearranged] state-atom]]]]))

(defn proposals-page []
  (get-logged-in-user-proposals-from-server)
  (fn []
    (let [user (session/get :user "NOBODY")]
      (set-title! "Proposals")
      [:div.row
       [:div.col-md-12
        [:div.row
         [:h2 "Proposals:"]
         [:p (str "Logged in: " (:username user))]]
        [:div.row
         [:div.col-md-8
          [logged-in-user-proposals-display]
          ;(let [curr-genre (str (session/get-in [:current-proposal :genre]))]
          ;  (case curr-genre
          ;    "music" [performance-proposal-questions]
          ;    "dance" [performance-proposal-questions]
          ;    "spokenword" [performance-proposal-questions]
          ;    "theater" [performance-proposal-questions]
          ;    [:p "(no performance aspect)"]))
          ;(let [curr-genre (str (session/get-in [:current-proposal :genre]))]
          ;  (case curr-genre
          ;    "music" [music-proposal-questions]
          ;    "dance" [dance-proposal-questions]
          ;    "film" [film-proposal-questions]
          ;    "spokenword" [spokenword-proposal-questions]
          ;    "visualarts" [visualarts-proposal-questions]
          ;    "theater" [theater-proposal-questions]
          ;    [:p "no further questions, your honor"]))
]
         [:div.col-md-4
          [:p "users associated with this proposal"]]]]])))
          ;[:p "proposals with common users/members"]


