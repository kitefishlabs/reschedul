(ns reschedul.pages.proposals
  (:require [reagent.core :as r]
            [reschedul.util :refer [set-title!
                                    empty-all-string-values
                                    error-handler]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]))

(defonce state-atom (r/atom {:editing? false :saved? true :loaded? false}))

(defn create-proposal-on-server! []
  (let [empty-proposal {:_id "-1" :title "TITLE" :category "none" :proposer-id (session/get-in [:user :_id])}]
    (.log js/console (str empty-proposal)
    (POST "/api/proposal"
          {:params empty-proposal
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "create-proposal-to-server success resp: " resp))
                      ;; TODO: -> add to recents?
                      (session/assoc-in! [:current-proposal] resp)
                      (swap! state-atom assoc-in [:saved] false))}))))

(defn create-proposal-info-on-server! []
  (let [empty-proposal {:_id "-1" :proposer-id (session/get-in [:user :_id]) :proposal-id "-1"}]
    (.log js/console (str empty-proposal)
          (POST "/api/proposal"
                {:params empty-proposal
                 :error-handler error-handler
                 :response-format :json
                 :keywords? true
                 :handler (fn [resp]
                            (.log js/console (str "create-proposal-info-to-server success resp: " resp))
                            (session/assoc-in! [:current-proposal-info] resp)
                            (swap! state-atom assoc-in [:saved] false))}))))



(defn save-proposal-to-server []
  (let [current-proposal (session/get-in [:current-proposal])]
    (.log js/console (str "save-proposal-to-server: " current-proposal))
    (if (not-empty current-proposal)
      (POST (str "/api/proposal/" (:_id current-proposal))
            {:params current-proposal
             :error-handler #(.log js/console (str "save-proposal-to-server ERROR" %))
             :response-format :json
             :keywords? true
             :handler (fn [resp]
                        (.log js/console (str "save-proposal-to-server success resp: " resp))
                        ;force the send of the user to the server?
                        ; better - add to recents!
                        (session/assoc-in! [:current-proposal] resp)
                        (swap! state-atom assoc-in [:saved?] true))}))))

(defn get-logged-in-user-proposals-from-server []
  (let [user (session/get-in [:user])
        username (:username user)]
    (.log js/console (str "get-logged-in-user-proposal-from-server: /api/proposals/user/" username))
    (GET (str "/api/proposal/user/" username)
         {
          :error-handler #(.log js/console (str "get-logged-in-proposal-from-server ERROR" %))
          :response-format :json
          :keywords? true
          :handler (fn [resp]
                     (.log js/console (str "get-logged-in-proposals-from-server success resp: " resp))
                     ;force the send of the user to the server?
                     ; better - add to recents!
                     (session/assoc-in! [:current-proposal] resp)
                     (swap! state-atom assoc-in [:saved] true))})
    (GET (str "/api/proposal-info/by-proposer/" (:_id user))
         {
          :error-handler #(.log js/console (str "get-logged-in-proposal-from-server ERROR" %))
          :response-format :json
          :keywords? true
          :handler (fn [resp]
                     (.log js/console (str "get-logged-in-proposals-from-server success resp: " resp))
                     ; force the send of the user to the server?
                     ;  better - add to recents!
                     (session/assoc-in! [:current-proposal] resp)
                     (swap! state-atom assoc-in [:saved] true))})
    ;(GET (str "/api/proposal-info/" username)
    ;     {:params {:username username}
    ;      :error-handler #(.log js/console (str "get-logged-in-proposal-from-server ERROR" %))
    ;      :response-format :json
    ;      :keywords? true
    ;      :handler (fn [resp]
    ;                 (.log js/console (str "get-logged-in-proposals-from-server success resp: " resp))
                     ;; force the send of the user to the server?
                     ;;  better - add to recents!
                     ;(session/assoc-in! [:current-proposal] resp)
                     ;(swap! state-atom assoc-in [:saved] true))})
    ))


(defn get-proposal-from-server []
  (let [proposals (session/get-in [:proposals])
        first-proposal (first proposals)]
    (.log js/console (str "get-proposal-from-server: " first-proposal))
    (GET (str "/api/proposal/" (:_id first-proposal))
         {
          :error-handler #(.log js/console (str "get-proposal-from-server ERROR" %))
          :response-format :json
          :keywords? true
          :handler (fn [resp]
                     (.log js/console (str "get-proposal-from-server success resp: " resp))
                     ;force the send of the user to the server?
                     ; better - add to recents!
                     (session/assoc-in! [:current-proposal] resp))})))

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

(defn row-bool [label schema-kws]
  (fn []
    [:div.row.user-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (if (true? (session/get-in schema-kws)) "yes" "no"))]]
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
    (.log js/console (str "esbm: " schema-kws))
    [:div.row.user-schema-boolean-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:select
                                   {;:placeholder "no"
                                    :multiple false
                                    :value (if (true? (session/get-in schema-kws)) "yes" "no")
                                    :on-change (fn [x]
                                                 (let [val (-> x .-target .-value)]
                                                   (.log js/console (str "->bool: " val))
                                                   (session/swap! assoc-in schema-kws (if (= "yes" val) true false))))}
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
                                           ;:placeholder "fudge"
                                           :rows 3
                                           :value (session/get-in schema-kws)
                                           :on-change #(session/swap! assoc-in schema-kws (-> % .-target .-value))}]]
     [:div.col-md-2 [group-state-icons :ok]]]))

;; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-dropdown-row [label schema-kws dropdown-list-map]
  (fn []
    [:div.row.user-schema-dropdown-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-8 ^{:key label}
      [:select
       {:data-placeholder "Choose a genre"
        :multiple false
        :value (session/get-in schema-kws)
        :on-change (fn [resp]
                     (let [curr (-> resp .-target .-value)]
                       (.log js/console (str "->tv: " curr))
                       (session/assoc-in! schema-kws curr)))}
       (for [pair dropdown-list-map]
         [:option {:key (first pair)} (second pair)])]]]))


(defn schema-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-row label schema-kws]
        [row label schema-kws state-atom])]]))

(defn schema-boolean-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-boolean-row label schema-kws]
        [row-bool label schema-kws state-atom])]]))

(defn schema-textarea-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-textarea-row label schema-kws]
        [row label schema-kws state-atom])]]))

(defn schema-dropdown-row [label schema-kws dropdown-list-map state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-dropdown-row label schema-kws dropdown-list-map]
        [row label schema-kws state-atom])]]))

(defn copy-user-to-primary-contact []
  (let [user (session/get-in [:user])]
    (do
      (session/assoc-in! [:current-proposal :primary-contact-name] (str (:first_name user) " " (:last_name user)))
      (session/assoc-in! [:current-proposal :primary-contact-email] (get-in user [:contact-info :email]))
      (session/assoc-in! [:current-proposal :primary-contact-phone] (get-in user [:contact-info :cell-phone])))))

(def rating-choices
  (array-map
    :g "G"
    :pg "PG"
    :r "R"
    :nc17 "NC17"))

(def category-choices
  ;"Choose the category"
  (array-map
    :music "music"
    :dance "dance"
    :film "film"
    :spokenword "spokenword"
    :visualart "visualart"
    :theater "theater"
    :none "none"))

(def available-choices
  "---"
  (array-map
    :all-day "all-day"
    :12noon "12noon"
    :1pm "1pm"
    :2pm "2pm"
    :3pm "3pm"
    :4pm "4pm"
    :5pm "5pm"
    :6pm "6pm"
    :7pm "7pm"
    :8pm "8pm"
    :9pm "9pm"
    :10pm "10pm"
    :11pm "11pm"
    :12midnight "12midnight"
    :1am "1am"
    :2am "2am"))

(defn available-day [day-date day-ky]
  (fn [day-date day-ky]
    (.log js/console "fire available day: available-day")
    [:div.row
     [:div.col-md-12
      [schema-boolean-row (str day-date " (y/n)?") [:current-proposal :availability (keyword day-ky) :is-available?] state-atom]
      (if (true? (session/get-in [:current-proposal :availability (keyword day-ky) :is-available?]))
        [:div
         [schema-dropdown-row (str " -     from: ") [:current-proposal :availability (keyword day-ky) :start-time] available-choices state-atom]
         [schema-dropdown-row (str " -     to: ") [:current-proposal :availability (keyword day-ky) :end-time] available-choices state-atom]])]]))


(defn logged-in-user-availability-display []
  (fn []
    (.log js/console "fire: logged-in-user-availability-display")
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Availability for proposal: " (session/get-in [:current-proposal :title]))]
      [control-row :user state-atom]
      [group-state-icons]]
     [:div.panel-body
      [:div.col-md-12
       [available-day "Thursday 7/28" :thu-1]
       [available-day "Friday 7/29" :fri-1]
       [available-day "Saturday 7/30" :sat-1]
       [available-day "Sunday 7/31" :sun-1]]]]))

;; TODO: this needs to be wrapped by auth
(defn logged-in-user-proposals-display []
  (fn []
    (.log js/console (str @session/state))
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Basic information for proposal: " (session/get-in [:current-proposal :title]))]
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
          [:input
           ^{:key (:title proposal)}
           {:type "button"
            :value (:title proposal)
            :on-click #(session/assoc-in! [:current-proposal] proposal)}]])
       [schema-row "Proposal Title" [:current-proposal :title] state-atom]
       [schema-dropdown-row "Category" [:current-proposal :category] category-choices state-atom]
       [schema-row "Please list any genre tags/keywords." [:current-proposal :genre-tags] state-atom]
       [schema-row "Proposer" [:current-proposal :proposer] state-atom]

       [:button.btn.btn-xs.btn-primary {:type "button"
                                        :on-click #(copy-user-to-primary-contact)} "copy logged-in user to primary contact"]
       [schema-row "Primary contact name" [:current-proposal :primary-contact-name] state-atom]
       [schema-row "Primary contact email" [:current-proposal :primary-contact-email] state-atom]
       [schema-row "Primary contact phone" [:current-proposal :primary-contact-phone] state-atom]
       [schema-row "Primary contact's role/relationship?" [:current-proposal :primary-contact-role] state-atom]

       [schema-row "Secondary contact name" [:current-proposal :secondary-contact-name] state-atom]
       [schema-row "Secondary contact email" [:current-proposal :secondary-contact-email] state-atom]
       [schema-row "Secondary contact phone" [:current-proposal :secondary-contact-phone] state-atom]
       [schema-row "Secondary contact's role/relationship" [:current-proposal :secondary-contact-role] state-atom]
       ; AVAILABILITY@@@

       [schema-row "Number of performers." [:current-proposal :number-of-performers] state-atom]
       [schema-textarea-row "Performers' names and roles." [:current-proposal :performers-names] state-atom]
       [schema-row "Are any members of your group in other proposals? If so, please list." [:current-proposal :potential-conflicts] state-atom]

       [schema-textarea-row "Description (for organizers - private)" [:current-proposal :description-private] state-atom]
       [schema-textarea-row "Description (for publicity - public)" [:current-proposal :description-public] state-atom]
       [schema-textarea-row "Description (140 chars MAX, for newspaper schedule)" [:current-proposal :description-public-140] state-atom]
       [schema-textarea-row "General notes to the organizers." [:current-proposal :general-notes] state-atom]

       ;[schema-row "Assigned organizer(s)" [:current-proposal :assigned-organizers] state-atom]
       ]]]))


(defn performance-proposal-questions []
  (fn []
    (let [category (session/get-in [:current-proposal :category])]
      (if (or (= category "music") (= category "dance") (= category "theater") (= category "film") (= category "spokenword"))
        [:div.panel.panel-default
         [:div.panel-heading
          [:h4 (str "Additional questions for performances...")]
          [control-row :current-proposal state-atom]
          ;[group-state-icons]
          ]
         [:div.panel-body
          [:div.col-md-12
           [schema-row "Setup time?" [:proposal :performance :setup-time] state-atom]
           [schema-row "Ideal performance duration?" [:proposal :performance :run-time] state-atom]
           [schema-row "Teardown time?" [:proposal :performance :teardown-time] state-atom]
           [schema-dropdown-row "Approximate rating?" [:proposal :rating] rating-choices state-atom]
           [schema-boolean-row "Are all performers 21+?" [:proposal :twentyone?] state-atom]
           [schema-boolean-row "Does your act require seating?" [:proposal :seating?] state-atom]
           [schema-boolean-row "Do you project images?" [:proposal :projection-self?] state-atom]
           [schema-boolean-row "Can other artists project images during your performance?" [:proposal :projection-other?] state-atom]
           [schema-textarea-row "Preferred venue?" [:proposal :space-preferred] state-atom]
           [schema-textarea-row "Do you have a space prearranged? If so, please describe." [:proposal :space-prearranged] state-atom]
           [schema-boolean-row "Can other performers share your space?" [:proposal :share-space?] state-atom]
           [schema-boolean-row "Are you willing to perform at opening/closing ceremonies? (Note: these are our only 2 fundraiser events.)" [:proposal :opening-ceremonies?] state-atom]
           [schema-textarea-row "Are there any performers with whom you would like to perform? A specific group show idea? Please explain." [:proposal :group-proposal-ideas] state-atom]
           [schema-textarea-row "Are there any venues at which you would prefer NOT to perform? Please explain." [:proposal :venues-not-perform] state-atom]]]]))))

;
(defn music-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "music")
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Additional questions for music proposals...")]
      [control-row :current-proposal state-atom]
      ;[group-state-icons]
      ]
     [:div.panel-body
      [:div.col-md-12
       [schema-row "Number of performances (max 3 indoor)." [:proposal :inside-performances] state-atom]
       [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
       [schema-textarea-row "What is the minimum amount of space for your performance." [:proposal :space-needs-minimum] state-atom]
       [schema-textarea-row "Describe your power needs/equipment." [:proposal :power-needs] state-atom]
       [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :amp-needs] state-atom]
       [schema-textarea-row "Can you provide drums and/or backline amps to a group show?" [:proposal :drums-backline-to-provide] state-atom]
       [schema-boolean-row "Does your act require a full sound system?" [:proposal :full-sound-system?] state-atom]
       [schema-textarea-row "Do you have other equipment you are willing to share?" [:proposal :gear-to-share] state-atom]
       [schema-row "How loud are you? (1-10)" [:proposal :how-loud] state-atom]
       [schema-row "Anything we should know about your setup/tech?" [:proposal :setup-notes] state-atom]]]])))


(defn dance-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "dance")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for dance proposals...")]
        [control-row :current-proposal state-atom]
        ;[group-state-icons]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Number of performances" [:proposal :inside-performances] state-atom]
         [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." [:proposal :space-needs-minimum] state-atom]
         [schema-textarea-row "Describe your power needs/equipment." [:proposal :power-needs] state-atom]
         [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :amp-needs] state-atom]
         [schema-boolean-row "Does your act require a sound system?" [:proposal :basic-sound-system?] state-atom]
         [schema-boolean-row "Does your act require seating?" [:proposal :seating-needed?] state-atom]
         [schema-textarea-row "Can other performers share your gear? If so, what?" [:proposal :gear-to-share] state-atom]]]])))


(defn spokenword-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "spokenword")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for spoken word and poetry proposals...")]
        [control-row :current-proposal state-atom]
        ;[group-state-icons]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Number of performances" [:proposal :inside-performances] state-atom]
         [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." [:proposal :space-needs-minimum] state-atom]
         [schema-textarea-row "Describe your power needs/equipment." [:proposal :tech :power-needs] state-atom]
         [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :tech :amp-needs] state-atom]
         [schema-boolean-row "Does your act require a sound system?" [:proposal :tech :basic-sound-system?] state-atom]
         [schema-boolean-row "Does your act require seating?" [:proposal :tech :seating-needed?] state-atom]
        [schema-textarea-row "Do you have other equipment you are willing to share?" [:proposal :gear-to-share] state-atom]]]])))


(defn film-proposal-questions []
  (fn []
    ;(.log js/console (str "film: " (session/get-in [:current-proposal :category]) "film"))
    (if (= (session/get-in [:current-proposal :category]) "film")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for film and video proposals...")]
        [control-row :current-proposal state-atom]
        ;[group-state-icons]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Number of screenings" [:proposal :inside-performances] state-atom]
         [schema-boolean-row "Live Performance?" [:proposal :live-performance?] state-atom]
         [schema-boolean-row "Installation?" [:proposal :installation?] state-atom]
         [schema-row "Genre" [:proposal :film-genre] state-atom]
         [schema-row "Duration" [:proposal :film-duration] state-atom]
         [schema-textarea-row "Preview urls + instructions." [:proposal :preview-urls] state-atom]
         [schema-textarea-row "Can you provide a viewing space? If so, please describe." [:proposal :can-facilitate-screening] state-atom]
         [schema-textarea-row "Can you provide a projector and/or screen?" [:proposal :can-provide-projector] state-atom]]
        [:div.col-md-12 [:div.row [:p "n/a"]]]]])))


(defn theater-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "theater")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for theater proposals...")]
        [control-row :current-proposal state-atom]
        ;[group-state-icons]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Number of performances" [:proposal :inside-performances] state-atom]
         [:p "For non-traditional (and possibly traditional) spaces..."]
         [schema-textarea-row "Describe your space needs." [:proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." [:proposal :space-needs-minimum] state-atom]
         [schema-textarea-row "Describe your power needs/equipment." [:proposal :power-needs] state-atom]
         [schema-textarea-row "Describe your amplification needs/equipment." [:proposal :amp-needs] state-atom]
         [schema-boolean-row "Does your act require a basic sound system (for cd/mp3/laptop)?" [:proposal :basic-sound-system?] state-atom]
         [schema-boolean-row "Does your act require seating?" [:proposal :seating-needed?] state-atom]
         [schema-textarea-row "Can other performers share your gear? If so, what?" [:proposal :gear-to-share] state-atom]]]])))


(defn visualarts-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :genre]) "visualarts")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for visual arts proposals...")]
        [control-row :current-proposal state-atom]
;      [group-state-icons]
        ]
       [:div.panel-body
        [:div.col-md-12
        [schema-boolean-row "Live Performance?" [:proposal :live-performance?] state-atom]
        [schema-boolean-row "Installation?" [:proposal :installation?] state-atom]
        [schema-row "Number of pieces?" [:proposal :number-of-pieces] state-atom]
        [schema-textarea-row "List pieces and sizes." [:proposal :pieces-list] state-atom]
        [schema-textarea-row "Do you have gallery space prearranged? If so, what space?" [:proposal :prearranged] state-atom]
        [schema-textarea-row "Describe your space needs, beyond wall space." [:proposal :space-needs] state-atom]
        [schema-textarea-row "What is the minimum amount of space for your art/installation/piece." [:proposal :space-needs-minimum] state-atom]]]])))


(defn proposals-page []
  (get-logged-in-user-proposals-from-server)
  (fn []
    (let [user (session/get :user)]
      (set-title! "Proposals")
      [:div.row
       [:div.col-md-12
        [:div.row
         [:h2 "Proposals"]
         [:p (str "Logged in: " (:username user))]]
        [:div.row
         [:div.col-md-8
          [:p (str (session/get-in [:current-proposal :genre]))]
          [logged-in-user-proposals-display]
          [logged-in-user-availability-display]
          [music-proposal-questions]
          [dance-proposal-questions]
          [film-proposal-questions]
          [spokenword-proposal-questions]
          [visualarts-proposal-questions]
          [theater-proposal-questions]
          [performance-proposal-questions]]
         [:div.col-md-4
          [:p "users associated with this proposal"]
          [:p "proposals with common users/members"]]]]])))



