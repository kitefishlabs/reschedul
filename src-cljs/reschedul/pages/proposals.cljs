(ns reschedul.pages.proposals
  (:require [reagent.core :as r]
            [reschedul.util :refer [set-title!
                                    empty-all-string-values
                                    error-handler]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]))

(defonce state-atom (r/atom {:editing? true :saved? true :proposal-submitted? false}))

(defn create-proposal-on-server! []
  (let [empty-proposal {:_id "-1" :title "TITLE" :category "none" :proposer-username (session/get-in [:user :username]) :assigned-organizer-username "admin"}]
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
                      (swap! state-atom assoc-in [:saved?] true))}))))

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
                            (swap! state-atom assoc-in [:saved?] true))}))))



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
    (.log js/console (str "get-logged-in-user-proposal-from-server: /api/proposal/user/" username))
    (GET (str "/api/proposal/user/" username)
         {
          :error-handler #(.log js/console (str "get-logged-in-proposal-from-server ERROR" %))
          :response-format :json
          :keywords? true
          :handler (fn [resp]
                     (.log js/console (str "get-logged-in-proposals-from-server success resp: " (first resp)))
                     (.log js/console (str "get-logged-in-proposals-from-server success resp: " (rest resp)))
                     ;force the send of the user to the server?
                     ; better - add to recents!
                     (session/assoc-in! [:current-proposal] (first resp))
                     (session/assoc-in! [:other-proposals] (filter (fn [k v] (or (= (keyword k) :_id) (= (keyword k) :title))) (rest resp)))
                     (swap! state-atom assoc-in [:saved?] true))})))

;(GET (str "/api/proposal/availability-info/" username)
;     {:params {:username username}
;      :error-handler #(.log js/console (str "get-logged-in-availability-info-from-server ERROR" %))
;      :response-format :json
;      :keywords? true
;      :handler (fn [resp]
;                 (.log js/console (str "get-logged-in-availability-info-from-server success resp: " resp))
                     ;; force the send of the user to the server?
                     ;;  better - add to recents!
                     ;(session/assoc-in! [:current-availability] resp)
                     ;(swap! state-atom assoc-in [:saved?] true))})



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
                     (session/assoc-in! [:current-proposal] resp)
                     (swap! state-atom assoc-in [:saved?] true))})))

(defn control-row [state-atom]
  (fn []
    [:div.row
     [:p
      [:span {:display :inline} "EDIT?:  "]
      [:input {:type "checkbox"
               :value (:editing? @state-atom)
               :on-click #(swap! state-atom update-in [:editing?] not)}]
      [:input {:type "button"
               :value (if (:saved? @state-atom) (str "Saved.") (str "*Save?"))
               :on-click #(save-proposal-to-server)}]]]))

(defn hints-pane [schema-kws]
  (let [state :nothing]
    (fn [schema-kws]
      [:div.row
       (cond
         (= state :ok) [:p {:style {:backgroundColor "green"}} "."]
         (= state :warn) [:p {:backgroundColor  "yellow"} "."]
         (= state :invalid) [:p {:backgroundColor  "red"} "."]
         :else [:p {:backgroundColor "white"} "-"])]))) ;:span {:style {:color "black"}}

(defn row [label schema-kws]
  (fn []
    [:div.row.user-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (session/get-in schema-kws))]]
     [:div.col-md-2
      [hints-pane schema-kws]]]))

(defn row-bool [label schema-kws]
  (fn []
    [:div.row.user-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:span (str (if (true? (session/get-in schema-kws)) "yes" "no"))]]
     [:div.col-md-2
      [hints-pane schema-kws]]]))

; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
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


; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-textarea-row [label hint schema-kws]
  (fn []
    [:div.row.user-schema-textarea-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label} [:input {:placeholder hint
                                           :type "textarea"
                                           :class "form-control"
                                           :maxLength 1000
                                           ;:placeholder "fudge"
                                           :rows 3
                                           :value (session/get-in schema-kws)
                                           :on-change (fn [x]
                                                        (session/swap! assoc-in schema-kws (-> x .-target .-value))
                                                        (swap! state-atom assoc-in [:saved?] false))}]]
     [:div.col-md-2 [hints-pane schema-kws]]]))

;; NOTE: session-keyword == schema-kw, i.e. the symbol name for the schema
(defn edit-schema-dropdown-row [label schema-kws dropdown-list-map]
  (fn []
    [:div.row.user-schema-dropdown-row
     [:div.col-md-4 [:span label]]
     [:div.col-md-6 ^{:key label}
      [:select
       {:data-placeholder "Choose a genre"
        :multiple false
        :value (session/get-in schema-kws)
        :on-change (fn [resp]
                     (let [curr (-> resp .-target .-value)]
                       ;(.log js/console (str "->tv: " curr))
                       (session/assoc-in! schema-kws curr)
                       (swap! state-atom assoc-in [:saved?] false)))}
       (for [pair dropdown-list-map]
         [:option {:key (first pair)} (second pair)])]]
     [:div.col-md-2 [hints-pane schema-kws]]]))


(defn schema-row [label hint schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if true
        [edit-schema-row label hint schema-kws]
        [row label schema-kws state-atom])]]))

(defn schema-boolean-row [label schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-boolean-row label schema-kws]
        [row-bool label schema-kws state-atom])]]))

(defn schema-textarea-row [label hint schema-kws state-atom]
  (fn []
    [:div.row
     [:div.col-md-12
      (if (get-in @state-atom [:editing?])
        [edit-schema-textarea-row label hint schema-kws]
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
      [control-row state-atom]
      [hints-pane]]
     [:div.panel-body
      [:div.col-md-12
       [available-day "Thu 7/28" :thu-1]
       [available-day "Fri 7/29" :fri-1]
       [available-day "Sat 7/30" :sat-1]
       [available-day "Sun 7/31" :sun-1]
       [available-day "Mon 8/1" :mon-2]
       [available-day "Tue 8/2" :tue-2]
       [available-day "Wed 8/3" :wed-2]
       [available-day "Thu 8/4" :thu-2]
       [available-day "Fri 8/5" :fri-2]
       [available-day "Sat 8/6" :sat-2]
       [available-day "Sun 8/7" :sun-2]]]]))

;; TODO: this needs to be wrapped by auth
(defn logged-in-user-proposals-display []
  ;(let [edit? (r/atom true)]
    (fn []
      ;(.log js/console (str @session/state))

      [:div.panel.panel-default
       [:div.panel-heading
          [:h4 (str "Basic information for proposal: " (session/get-in [:current-proposal :title]))]
        [control-row state-atom]
        [hints-pane]]
       [:div.panel-body
        [:div.col-md-12
         [:input {:name "create"
                  :type "button"
                  :value "create"
                  :on-click #(create-proposal-on-server!)}]
         ;[:input.toggle {
         ;         :type "checkbox"
         ;         :checked @edit?
         ;         :on-change #(swap! edit? not)}]
         (for [proposal (session/get-in [:proposals])]
           [:div.row
            [:input
             ^{:key (:title proposal)}
             {:type "button"
              :value (:title proposal)
              :on-click #(session/assoc-in! [:current-proposal] proposal)}]])
         [schema-row "Proposal Title" "Title as it will appear in the schedule." [:current-proposal :title] state-atom]
         [schema-dropdown-row "Category" [:current-proposal :category] category-choices state-atom]
         [schema-row "Please list any genre tags/keywords." "e.g. #funk #contact-improv etc." [:current-proposal :genre-tags] state-atom]
         [schema-row "Proposer" "HINT" [:current-proposal :proposer-username] state-atom]
         [schema-row "Assigned organizer(s)" "Assigned by admins/organizers." [:current-proposal :assigned-organizer-username] state-atom]

         [schema-row "Primary contact name" "Who is our main contact?" [:current-proposal :primary-contact-name] state-atom]
         [schema-row "Primary contact email" "We will communicate mainly through email." [:current-proposal :primary-contact-email] state-atom]
         [schema-row "Primary contact phone" "We also need may need to call you with questions or to confirm your schedule." [:current-proposal :primary-contact-phone] state-atom]
         [schema-row "Primary contact's role?" "e.g. lead singer, manager, etc." [:current-proposal :primary-contact-role] state-atom]

         [schema-row "Secondary contact name" "In case we cannot contact you directly. (optional for solo performers.)" [:current-proposal :secondary-contact-name] state-atom]
         [schema-row "Secondary contact email" "Only if we cannot reach the primary contact." [:current-proposal :secondary-contact-email] state-atom]
         [schema-row "Secondary contact phone" "We will call if we cannot reach the primary contact." [:current-proposal :secondary-contact-phone] state-atom]
         [schema-row "Secondary contact's role/relationship" "e.g. Drummer's mom, etc." [:current-proposal :secondary-contact-role] state-atom]
         ; AVAILABILITY@@@

         [schema-row "Desired number of performers." "The total number of performers. If you're not sure, estimate." [:current-proposal :number-of-performers] state-atom]
         [schema-textarea-row "Performers' names and roles." "As they should appear in publicity/promo material." [:current-proposal :performers-names] state-atom]
         [schema-row "Are any members of your group in other proposals?." "List names and their other groups/bands." [:current-proposal :potential-conflicts] state-atom]

         [schema-textarea-row "Description (for organizers - private)" "Describe your act and anything we need to know that doesn't fit elsewhere." [:current-proposal :description-private] state-atom]
         [schema-textarea-row "Description (for publicity - public)" "This text will appear in the online schedule." [:current-proposal :description-public] state-atom]
         [schema-textarea-row "Description (140 chars MAX, for newspaper schedule)" "Strict limit for publication. 140 chars, max." [:current-proposal :description-public-140] state-atom]
         [schema-textarea-row "General notes to the organizers." "Any thing else we should know?" [:current-proposal :general-notes] state-atom]
         [schema-boolean-row "I would like to be scheduled for street/outdoor performances." [:current-proposal :outside-willing?] state-atom]]]]))


(defn performance-proposal-questions []
  (fn []
    (let [category (session/get-in [:current-proposal :category])]
      (if (or (= category "music") (= category "dance") (= category "theater") (= category "film") (= category "spokenword"))
        [:div.panel.panel-default
         [:div.panel-heading
          [:h4 (str "Additional questions for performances...")]
          [control-row state-atom]
          ;[hints-pane]
          ]
         [:div.panel-body
          [:div.col-md-12
           [schema-row "Setup time?" "In minutes" [:current-proposal :setup-time] state-atom]
           [schema-row "Ideal performance duration?" "In minutes, explain issues in notes"[:current-proposal :run-time] state-atom]
           [schema-row "Teardown time?" "In minutes"[:current-proposal :teardown-time] state-atom]
           ; Are they kid friendly?
           [schema-dropdown-row "Approximate rating?" [:current-proposal :rating] rating-choices state-atom] ; "Most will be PG. Mark kid-friendly ones G!"
           [schema-boolean-row "Are all performers 21+?" [:current-proposal :twentyone?] state-atom] ;"For shows at bars"
           [schema-boolean-row "Does your act require seating?" [:current-proposal :seating?] state-atom] ; "Varies widely by venue. No guarentees."
           [schema-boolean-row "Do you project images?" [:current-proposal :projection-self?] state-atom]
           [schema-boolean-row "Can other artists project images during your performance?" [:current-proposal :projection-other?] state-atom]
           [schema-textarea-row "Preferred venue?" "We take suggestions." [:current-proposal :space-preferred] state-atom]
           [schema-textarea-row "Do you have a space prearranged? If so, please describe." "You know who you are" [:current-proposal :space-prearranged] state-atom]
           [schema-boolean-row "Can other performers share your space?" [:current-proposal :share-space?] state-atom]
           [schema-boolean-row "Are you willing to perform in non-traditional settings? (porches, backyards, etc.)" [:current-proposal :opening-ceremonies?] state-atom]
           [schema-boolean-row "Are you willing to perform at opening/closing ceremonies? (Note: these are our only 2 fundraiser events, door $ goes to festival.)" [:current-proposal :opening-ceremonies?] state-atom]
           [schema-textarea-row "Are there any performers with whom you would like to perform? A specific group show idea? Please explain." "Help us schedule you." [:current-proposal :group-proposal-ideas] state-atom]
           [schema-textarea-row "Are there any venues at which you would prefer NOT to perform? Please explain." "Uh..." [:current-proposal :venues-not-perform] state-atom]]]]))))


(defn music-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "music")
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Additional questions for music proposals...")]
      [control-row state-atom]
      ;[hints-pane]
      ]
     [:div.panel-body
      [:div.col-md-12
       [schema-row "Desired number of indoor performances (MAX 3)." "You can do more if you are a street performer." [:current-proposal :inside-performances] state-atom]
       [schema-textarea-row "Describe your space needs." "Give real measurements if you can or reference local stages." [:current-proposal :space-needs] state-atom]
       [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
       [schema-textarea-row "Describe your power needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :power-needs] state-atom]
       [schema-textarea-row "Describe your amplification needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :amp-needs] state-atom]
       [schema-textarea-row "Can you provide drums and/or backline amps to a group show?" "Please explain. Can we put you in charge of a show?" [:current-proposal :drums-backline-to-provide] state-atom]
       [schema-boolean-row "Does your act require a full sound system?" [:current-proposal :full-sound-system?] state-atom] ;"This determines where we can schedule you."
       [schema-textarea-row "Do you have other equipment you are willing to share?" "Full PAs? Lights?" [:current-proposal :gear-to-share] state-atom]
       [schema-row "How loud are you? (1-10)" "Using our scientific scale." [:current-proposal :how-loud] state-atom]
       [schema-textarea-row "Anything we should know about your setup/tech?" "Be detailed!" [:current-proposal :setup-notes] state-atom]]]])))

(defn street-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :ouside-willing?]) "street")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for street proposals...")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-boolean-row "Would you like to busk (rather than being scheduled for performances.)?" [:current-proposal :outide-busk?] state-atom]
         [schema-boolean-row "Do you have/can you obtain a busking license?" [:current-proposal :outside-license?] state-atom]
         [schema-boolean-row "Do you have experience busking?" [:current-proposal :outside-experience?] state-atom]
         [schema-row "Desired number of street/outdoor performances." "" [:current-proposal :outside-performances] state-atom]
         [schema-boolean-row "Does your act roam?" [:current-proposal :outside-roam?] state-atom]
         [schema-boolean-row "Does your act rely on interaction and/or improv?" [:current-proposal :outside-interaction?] state-atom]
         [schema-boolean-row "If necessary, do you have a mobile power source?" [:current-proposal :outside-battery?] state-atom]]]])))
         



(defn dance-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "dance")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for dance proposals...")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Desired number of performances" "No guarentees." [:current-proposal :inside-performances] state-atom]
         [schema-textarea-row "Describe your space needs." "Give real measurements if you can or reference local stages." [:current-proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
         [schema-textarea-row "Describe your power needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :power-needs] state-atom]
         [schema-textarea-row "Describe your amplification needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :amp-needs] state-atom]
         [schema-boolean-row "Does your act require a sound system?" [:current-proposal :basic-sound-system?] state-atom] ;"A few select venues can provide them."
         [schema-textarea-row "Can other performers share your gear? If so, what?" "Mainly tech-related." [:current-proposal :gear-to-share] state-atom]]]])))


(defn spokenword-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "spokenword")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for spoken word and poetry proposals...")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Desired number of performances" "No guarentees." [:current-proposal :inside-performances] state-atom]
         [schema-textarea-row "Describe your space needs." "Give real measurements if you can or reference local stages." [:current-proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
         [schema-textarea-row "Describe your power needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :tech :power-needs] state-atom]
         [schema-textarea-row "Describe your amplification needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :tech :amp-needs] state-atom]
         [schema-boolean-row "Does your act require a sound system?" [:current-proposal :tech :basic-sound-system?] state-atom] ;"We cannot provide anything aside from a few select venues."
         [schema-textarea-row "Do you have other equipment you are willing to share?" "If so, what?" [:current-proposal :gear-to-share] state-atom]]]])))


(defn film-proposal-questions []
  (fn []
    ;(.log js/console (str "film: " (session/get-in [:current-proposal :category]) "film"))
    (if (= (session/get-in [:current-proposal :category]) "film")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for film and video proposals...")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Desired number of screenings" "No guarentees." [:current-proposal :inside-performances] state-atom]
         [schema-boolean-row "Live Performance?" [:current-proposal :live-performance?] state-atom]
         [schema-boolean-row "Installation?" [:current-proposal :installation?] state-atom]
         [schema-row "Genre" "Of the film." [:current-proposal :film-genre] state-atom]
         [schema-row "Duration" "In minutes." [:current-proposal :film-duration] state-atom]
         [schema-textarea-row "Preview urls + instructions." "Indicate private videos. We'll ask you later about PR." [:current-proposal :preview-urls] state-atom]
         [schema-textarea-row "Can you provide a viewing space?" "If so, please describe." [:current-proposal :can-facilitate-screening] state-atom]
         [schema-textarea-row "Can you provide a projector and/or screen?" "And run it for a screening?" [:current-proposal :can-provide-projector] state-atom]]
        [:div.col-md-12 [:div.row [:p "n/a"]]]]])))


(defn theater-proposal-questions []
  (fn []
    (if (= (session/get-in [:current-proposal :category]) "theater")
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "Additional questions for theater proposals...")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        [:div.col-md-12
         [schema-row "Desired number of performances" "No guarentees." [:current-proposal :inside-performances] state-atom]
         [:p "For non-traditional (and possibly traditional) spaces..."]
         [schema-textarea-row "Describe your space needs." "Give real measurements if you can or reference local stages." [:current-proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
         [schema-textarea-row "Describe your power needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :power-needs] state-atom]
         [schema-textarea-row "Describe your amplification needs/equipment." "We cannot provide anything aside from a few select venues." [:current-proposal :amp-needs] state-atom]
         [schema-boolean-row "Does your act require a basic sound system (for cd/mp3/laptop)?" [:current-proposal :basic-sound-system?] state-atom] ;"We cannot provide anything aside from a few select venues."
         [schema-textarea-row "Can other performers share your gear?" "If so, what?" [:current-proposal :gear-to-share] state-atom]]]])))
;
;
(defn visualart-proposal-questions []
  (if (= (session/get-in [:current-proposal :category]) "visualart")
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Additional questions for visual arts proposals...")]
      [control-row state-atom]
      ;[hints-pane]
      ]
     [:div.panel-body
      [:div.col-md-12
       [schema-textarea-row "List pieces and sizes." "Title, Medium, Width/Height/Depth in inches." [:current-proposal :pieces-list] state-atom]
       [schema-textarea-row "Do you have gallery space prearranged?" "If so, what space?" [:current-proposal :gallery-prearranged] state-atom]
       [schema-textarea-row "Describe your space needs, beyond wall space." "Especially if you have installation or 3D art." [:current-proposal :space-needs] state-atom]
       [schema-textarea-row "What is the minimum amount of space for your art/installation/piece." "If you are flexible. Otherwise we'll use the above info." [:current-proposal :space-needs-minimum] state-atom]
       [:p "Let us know if the following apply..."]
       [schema-boolean-row "Is there a live performance aspect?" [:current-proposal :live-performance?] state-atom]
       [schema-boolean-row "Is this an installation work?" [:current-proposal :installation?] state-atom]]]]))

(defn proposal-aggreement []
  (fn []
    (if (not (:proposal-submitted? @state-atom))
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "You must aggree to the following in order to submit.")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        [:div.col-md-12
         [:p "HEY! The Infringement Festival is an incredible experience that will help define Buffalo's Culture for generations. This incredible festival is only possible with the help and dedication of the organizers and volunteers who help make Infringement happen."]
         [:p "That being said, we need your help. Unlike other festivals of this size, we have less than a dozen year round organizers that make Infringement happen. To get the full understanding of the festivals' rules and values, make sure to check out our official Mandate."]
         [:p "By agreeing to the conditions below, you will be allowed to partake in what is possibly the most independent festival in the world!"]
         [:p "Your proposal submission is not complete until you have checked every box in this section. This proves to us you read an understand these statements."]
         [:h4 "Communication"]
         [schema-boolean-row "I agree to keep in touch with my genre organizer and venue contact." [:current-proposal :q1] state-atom]
         [schema-boolean-row "I will contact my genre organizer and venue organizer as soon as possible if I need to cancel a performance" [:current-proposal :q2] state-atom]
         [schema-boolean-row "I will check my e-mail and voicemail regularly, and respond to requests from my genre organizer as soon as possible." [:current-proposal :q3] state-atom]
         [schema-boolean-row "I understand that the Music Coordinator is Curt Rotterdam. I must add his email (steelcrazybooking@gmail.com) to my \"accepted senders list\" so that the emails do not go into my spam folder. I also understand that David Adamczyk is my Street Performance Coordinator and that I must add his e-mail (DGA8787@AOL.com) to my \"accepted senders list\" as well.I will stay in contact with them, knowing that if I don't, my proposal will be deleted." [:current-proposal :q4] state-atom]
         [:h4 "Respect"]
         [schema-boolean-row "I will conduct myself in a courteous and professional manner." [:current-proposal :q5] state-atom]
         [schema-boolean-row "I will treat the venue with the utmost care and I will leave it in the same condition as when I arrived." [:current-proposal :q6] state-atom]
         [schema-boolean-row "I understand that if I am scheduled into a group show with other acts, it is proper etiquette, time permitting, to arrive early and stay late to watch the other performers on the bill." [:current-proposal :q7] state-atom]
         [schema-boolean-row "I understand that this is a \"DIY\" festival and that any and all promotions for my proposal must come from myself." [:current-proposal :q8] state-atom]
         [:h4 "Meetings"]
         [schema-boolean-row "I will attend at least one planning/informational meeting before the festival. (If I live outside of Western New York or my schedule does not allow me to attend the regular meeting, I am responsible for contacting my genre organizer to schedule a one-on-one meeting." [:current-proposal :q9] state-atom]
         [:h4 "Flexibility"]
         [schema-boolean-row "I understand that I may have to be flexible with my schedule and my performances leading up to and during the festival. I understand that with a festival of this size, put together entirely by volunteers, there are bound to be some scheduling mishaps, etc. I understand that I may have to \"roll with the punches\" so to speak, and make the best of whatever happens during the festival." [:current-proposal :q10] state-atom]
         [:h4 "Money"]
         [schema-boolean-row "I understand that certain sound expenses may occur at some venues, and money may be taken from the door to pay for these costs." [:current-proposal :q11] state-atom]
         [schema-boolean-row "I understand that no performer is guaranteed financial compensation. Many shows are free, and the only way of compensation is pass the hat / donation." [:current-proposal :q12] state-atom]
         [schema-boolean-row "I understand that if I am selected to play with an out of town act during a group show, I may be asked to donate portions of my share from the door to the out of town act." [:current-proposal :q13] state-atom]
         [schema-boolean-row "I understand that I must be at my scheduled venue at least 45 minutes before my scheduled performance time." [:current-proposal :q14] state-atom]
         [schema-boolean-row "I understand I must be at my location early to start on time." [:current-proposal :q15] state-atom]
         [:h4 "Street Performances"]
         [schema-boolean-row "I understand that weather is always a factor." [:current-proposal :q16] state-atom]
         [schema-boolean-row "I understand that I must obtain a street-performer's permit to legally street-perform in Buffalo." [:current-proposal :q17] state-atom]
         [schema-boolean-row "I understand that if I do anything illegal during my, the Infringement Festival cannot do anything if I am asked to stop by the Buffalo Police Department." [:current-proposal :q18] state-atom]
         [schema-boolean-row "I understand that if busking, I will not perform at one location for more than 2 hours at a time and will share the space with other acts who would like to perform." [:current-proposal :q19] state-atom]
         [schema-boolean-row "I will do my best to cooperate with the reasonable requests of store-owners and people in the neighborhood." [:current-proposal :q20] state-atom] ]]])))




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
         [:div.col-md-12
          [:p (str (session/get-in [:current-proposal :genre]))]
          [logged-in-user-proposals-display]
          [logged-in-user-availability-display]
          [music-proposal-questions]
          [dance-proposal-questions]
          [film-proposal-questions]
          [spokenword-proposal-questions]
          [visualart-proposal-questions]
          [theater-proposal-questions]
          [street-proposal-questions]
          [performance-proposal-questions]
          [proposal-aggreement]]
          [:p "users associated with this proposal"]
          [:p "proposals with common users/members"]]]])))

