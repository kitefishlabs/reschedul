(ns reschedul.pages.proposals
  (:require [reagent.core :as r]
            [reschedul.util :refer [set-title!
                                    empty-all-string-values
                                    error-handler]]
            [reschedul.session :as session]
            [ajax.core :refer [GET POST]]
            [reschedul.pages.common :refer [control-row
                                            hints-pane
                                            schema-row
                                            schema-textarea-row
                                            schema-boolean-row
                                            schema-checkbox-row
                                            schema-dropdown-row]]
            [reschedul.validation :refer [validate-proposal-creation]]))

(defonce state-atom (r/atom {:editing? true :saved? true :proposal-submitted? false}))

(def required-proposal-creation-fields
  [:primary-contact-name :proposer-username :primary-contact-email :primary-contact-phone
   :primary-contact-method :primary-contact-zipcode :primary-contact-role :title :primary-genre :secondary-genre])




(defn create-proposal-on-server! []
  (let [initial-proposal (session/get-in [:current-proposal])]
    (.log js/console (str initial-proposal)
    (POST "/api/proposal"
          {:params initial-proposal
           :error-handler error-handler
           :response-format :json
           :keywords? true
           :handler (fn [resp]
                      (.log js/console (str "create-proposal-to-server success resp: " resp))
                      ;; TODO: -> add to recents?
                      (session/assoc-in! [:current-proposal] resp)
                      (swap! state-atom assoc-in [:saved?] true))}))))


(defn save-proposal-to-server! []
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


;(defn copy-user-to-primary-contact []
;  (let [user (session/get-in [:user])]
;    (do
;      (session/assoc-in! [:current-proposal :primary-contact-name] (str (:first_name user) " " (:last_name user)))
;      (session/assoc-in! [:current-proposal :primary-contact-email] (get-in user [:contact-info :email]))
;      (session/assoc-in! [:current-proposal :primary-contact-phone] (get-in user [:contact-info :cell-phone])))))

(def three-choices
  (array-map
    :1 "1"
    :2 "2"
    :3 "3"))

(def ten-choices
  (array-map
    :one "1"
    :two "2"
    :three "3"
    :four "4"
    :five "5"
    :six "6"
    :seven "7"
    :eight "8"
    :nine "9"
    :ten "10+"))

(def genre-choices
  (array-map
    :rock "rock" :elctronic "electronic" :acoustic "acoustic" :jazz "jazz"
    :blues "blues" :hip-hop "hip-hop" :dance "dance" :country "country" :folk "folk"
    :folk "folk" :punk "punk" :metal "metal" :noise "noise" :avant-garde "avant-garde"
    :soul-r-n-b "soul-r-n-b" :pop "pop" :world "world" :latin "latin" :classical"classical"
    :singer-songwriter "singer-songwriter" :jam "jam" :indie "indie" :americana "americana"
    :garage "garage" :hardcore "hardcore" :reggae "reggae" :ska "ska" :psychedelic "psychedelic"
    :christian "christian" :other "other"))

(def busking-choices
  (array-map
    :busk             "busk"
    :perform          "perform"
    :busk-and-perform "both"))

(def drum-kit-choices
  (array-map
    :no-kit "no kit"
    :small "small (3-4 piece)"
    :large "large (5-6 piece)"
    :huge "huge"))

(def rating-choices
  (array-map
    :g "G"
    :pg "PG"
    :r "R"
    :nc17 "NC17"))

(def contact-choices
  (array-map
    :phone "phone"
    :email "email"
    :facebook "facebook"))

(def group-size-choices
  (array-map
    :solo-duo "solo or duo"
    :band-no-drums "small band (no drums)"
    :full-band-drums "full band (with drums)"
    :large-ensemble "large-ensemble"
    :small-stage "small stage performance"
    :large-stage "large stage performance"))

(def category-choices
  ;"Choose the category"
  (array-map
    :none "none"
    :music "music"
    :dance "dance"
    :film "film"
    :spokenword "spokenword"
    :visualart "visualart"
    :theater "theater"))

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
  ; req fields for save
  (let [req-fields required-proposal-creation-fields]
    (fn []
      ;(.log js/console (str @session/state))

      [:div.panel.panel-default
       [:div.panel-heading
          [:h4 (str "Basic information for proposal: " (session/get-in [:current-proposal :title]))]
        [control-row state-atom save-proposal-to-server!]
        [hints-pane]]
       [:div.panel-body
        [:div.col-md-12
         [:p "All fields marked with \"**\" are required to create your proposal. All fields are required to submit your proposal. Enter \"n/a\" for any fields that you do believe to be relevant."]
         [schema-row "Primary contact name **" "Who is our main contact?" [:current-proposal :primary-contact-name] state-atom]
         [schema-row "Proposer username **" "Your username!" [:current-proposal :proposer-username] state-atom]
         ;[schema-row "Assigned organizer(s)" "Assigned by admins/organizers." [:current-proposal :assigned-organizer-username] state-atom] ;ORG ONLY

         [schema-row "Primary contact email **" "We will communicate mainly through email." [:current-proposal :primary-contact-email] state-atom]
         [schema-row "Primary contact phone **" "We also need may need to call you with questions or to confirm your schedule." [:current-proposal :primary-contact-phone] state-atom]
         [schema-dropdown-row "Prefered method **" [:current-proposal :primary-contact-method] contact-choices state-atom]
         [schema-row "Primary contact zip code **" "We like to know where you're coming from." [:current-proposal :primary-contact-zipcode] state-atom]
         [schema-row "Primary contact's role? **" "e.g. lead singer, manager, etc." [:current-proposal :primary-contact-role] state-atom]

         [schema-row "Secondary contact name" "In case we cannot contact you directly. (optional, but strongly recommended for solo performers.)" [:current-proposal :secondary-contact-name] state-atom]
         [schema-row "Secondary contact email" "Only if we cannot reach the primary contact." [:current-proposal :secondary-contact-email] state-atom]
         [schema-row "Secondary contact phone" "We will call if we cannot reach the primary contact." [:current-proposal :secondary-contact-phone] state-atom]
         [schema-dropdown-row "Prefered method" [:current-proposal :secondary-contact-method] contact-choices state-atom]
         [schema-row "Secondary contact's role." "e.g. Drummer's mom, etc." [:current-proposal :secondary-contact-role] state-atom]

         [schema-row "Facebook profile" "For your act/group" [:current-proposal :facebook-profile-url] state-atom] ;
         [schema-row "Website" "For your your act/group." [:current-proposal :website-url] state-atom]
         [schema-row "Image link" "Image URL for promotion." [:current-proposal :image-url] state-atom]
         [schema-row "Video link" "Video URL for promotion." [:current-proposal :video-url] state-atom]

         [schema-dropdown-row "Number of performers/group members." [:current-proposal :number-of-performers] ten-choices state-atom]
         [schema-textarea-row "List of performers' names and their specific roles." "As they should appear in publicity/promo material." [:current-proposal :performers-names] state-atom]
         [schema-row "Are you in any other proposals?." "List names of those groups/bands." [:current-proposal :potential-conflicts] state-atom]
         [schema-row "Are any members of your group in other proposals?." "List names and their other groups/bands." [:current-proposal :potential-conflicts] state-atom]

         [schema-row "Proposal Title **" "Title as it will appear in the schedule." [:current-proposal :title] state-atom]
         [schema-dropdown-row "Category **" [:current-proposal :category] category-choices state-atom]

         ; TODO: primary, secondary lists, tertiary user-made-tags

         [schema-textarea-row "Description (for organizers - private)" "Describe your act and anything we need to know that doesn't fit elsewhere. Please be detailed." [:current-proposal :description-private] state-atom]
         [schema-textarea-row "Description (for infringement website publicity)" "This text will appear in the online schedule. It will be viewable by the general public. It can be as long as you like." [:current-proposal :description-public] state-atom]
         [schema-textarea-row "Description (140 chars MAX, for newspaper schedule)" "Strict limit for publication. 140 chars, max." [:current-proposal :description-public-140] state-atom]
         [schema-textarea-row "General notes to the organizers." "Any thing else we should know?" [:current-proposal :general-notes] state-atom]
         ; Can you volunteer a few hours for us before/during the festival?
         ; Will anyone in this proposal need housing?
         [schema-textarea-row "How does your act define Infingement? Have you performed at past festivals?" "Help us place you in context a little." [:current-proposal :infringement-ideals] state-atom]
         [schema-boolean-row "Can you help us by volunteering a few hours at or before the festival?" [:current-proposal :infringement-ideals] state-atom]]]])))


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
           [schema-row "Performance duration." "In minutes." [:current-proposal :run-time] state-atom]
           [schema-row "Setup time." "In minutes" [:current-proposal :setup-time] state-atom]
           [schema-row "Teardown time." "In minutes" [:current-proposal :teardown-time] state-atom]
           [schema-dropdown-row "How large is your group?" [:current-proposal :group-size] group-size-choices state-atom]
           [schema-dropdown-row "Approximate rating?" [:current-proposal :rating] rating-choices state-atom] ; "Most will be PG. Mark kid-friendly ones G!"
           [schema-boolean-row "Are all performers 21+?" [:current-proposal :twentyone?] state-atom] ;"For shows at bars"
           [schema-boolean-row "Does your act require seating?" [:current-proposal :seating?] state-atom] ; "Varies widely by venue. No guarantees."
           [schema-boolean-row "Do you project images?" [:current-proposal :projection-self?] state-atom]
           [schema-boolean-row "Can other artists project images during your performance?" [:current-proposal :projection-other?] state-atom]
           [schema-textarea-row "Preferred venue?" "We take suggestions." [:current-proposal :space-preferred] state-atom]
           [schema-textarea-row "Do you have a space prearranged?" "If so, please describe." [:current-proposal :space-prearranged] state-atom]
           [schema-boolean-row "Can other performers share this space?" [:current-proposal :share-space?] state-atom]
           [schema-boolean-row "Are you willing to perform in non-traditional settings? (porches, backyards, etc.)" [:current-proposal :nontraditional-spaces?] state-atom]
           [schema-boolean-row "Are you willing to perform at opening/closing ceremonies? (Note: these are our only two fundraiser events with all door $ going to the festival.)" [:current-proposal :opening-ceremonies?] state-atom]
           [schema-textarea-row "Are there any performers with whom you would like to perform? A specific group show idea? Please explain." "Help us schedule you." [:current-proposal :group-proposal-ideas] state-atom]]]]))))


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
       [schema-dropdown-row "Desired number of indoor music performances." [:current-proposal :inside-number-of-performances] three-choices state-atom] ;"You can do more if you are a street performer."
       [:p "* You may also propose street performances, below."]

       [schema-dropdown-row "Primary Genre" [:current-proposal :primary-genre] genre-choices state-atom]
       [schema-dropdown-row "Secondary Genre" [:current-proposal :secondary-genre] genre-choices state-atom]
       [schema-row "Please list any genre tags/keywords." "e.g. #funk #contact-improv etc." [:current-proposal :genre-tags] state-atom]

       [schema-boolean-row "Do you own a PA that you can use for your show?" [:current-proposal :can-provide-pa?] state-atom] ;"We can put on more awesomer shows if we have some bands with PAs."
       [schema-boolean-row "Does your act require a full sound system?" [:current-proposal :full-sound-system?] state-atom] ;"This determines where we can schedule you."
       [schema-boolean-row "Can you perform acoustic?" [:current-proposal :acoustic-ok?] state-atom]
       [schema-boolean-row "Do you need a power outlet?" [:current-proposal :need-power?] state-atom]
       [schema-boolean-row "Can you run your show (watch the door, keep track of the schedule, MC, etc.)?" [:current-proposal :can-run-show?] state-atom] ;"We can put on more awesomer shows if we have some bands with PAs."
       [schema-boolean-row "Can you provide drums to share?" [:current-proposal :drums-to-share?] state-atom]
       [schema-boolean-row "Can you provide amps or other equipment to share?" [:current-proposal :gear-to-share?] state-atom]

       [schema-boolean-row "Are you a dj/turntablist?" [:current-proposal :dj?] state-atom]
       [schema-boolean-row "Do you have dj equipment you can share?" [:current-proposal :dj-gear-to-share?] state-atom]

       [schema-dropdown-row "Size of drum kit." [:current-proposal :drum-kit-size] drum-kit-choices state-atom]
       [schema-dropdown-row "How loud are you? (1-10)" [:current-proposal :how-loud] ten-choices state-atom] ;"Using our scientific scale."
       [schema-row "What is the minimum amount of space for your performance?" "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
       [schema-textarea-row "Anything we should know about your setup/tech?" "Please provide details about the gear you can share and any unusual aspects of your setup." [:current-proposal :setup-notes] state-atom]]]])))

(defn street-proposal-questions []
  (fn []
    (.log js/console "BOOM!")
    ;(.log js/console (session/get-in [:current-proposal :ouside-willing?]))
    ;(if (true? (session/get-in [:current-proposal :ouside-willing?]))
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4 (str "Additional questions for street proposals...")]
      [control-row state-atom]
      ;[hints-pane]
      ]
     [:div.panel-body
      [:div.col-md-12
       [schema-dropdown-row "Would you like to busk, perform, or both outside?" [:current-proposal :outide-busk-perform-preference] busking-choices state-atom]
       [schema-boolean-row "Do you have/can you obtain a busking license?" [:current-proposal :outside-license?] state-atom]
       [schema-boolean-row "Do you have experience busking?" [:current-proposal :outside-experience?] state-atom]
       [schema-boolean-row "Does your act roam?" [:current-proposal :outside-roam?] state-atom]
       [schema-boolean-row "Does your act rely on interaction and/or improv?" [:current-proposal :outside-interaction?] state-atom]
       [schema-boolean-row "If necessary, do you have a mobile power source?" [:current-proposal :outside-battery?] state-atom]]]]))




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
         [schema-row "Desired number of performances" "No guarantees." [:current-proposal :inside-performances] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
         [schema-boolean-row "Do you need a power outlet?" [:current-proposal :need-power?] state-atom]
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
         [schema-row "Desired number of performances" "No guarantees." [:current-proposal :inside-performances] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
         [schema-boolean-row "Do you need a power outlet?" [:current-proposal :need-power?] state-atom]
         [schema-boolean-row "Does your act require a sound system?" [:current-proposal :basic-sound-system?] state-atom] ;"A few select venues can provide them."
         [schema-textarea-row "Do you have any equipment you are willing to share?" "If so, what?" [:current-proposal :gear-to-share] state-atom]]]])))


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
         [schema-row "Desired number of screenings" "No guarantees." [:current-proposal :inside-performances] state-atom]
         [schema-boolean-row "Live Performance?" [:current-proposal :live-performance?] state-atom]
         [schema-boolean-row "Installation?" [:current-proposal :installation?] state-atom]
         [schema-row "Genre" "Of the film." [:current-proposal :film-genre] state-atom]
         [schema-row "Duration" "In minutes." [:current-proposal :film-duration] state-atom]
         [schema-textarea-row "Preview urls + instructions." "Indicate private videos. We'll ask you later about PR." [:current-proposal :preview-urls] state-atom]
         [schema-boolean-row "Can you provide a viewing space?" [:current-proposal :can-facilitate-screening?] state-atom]
         [schema-boolean-row "Can you provide a projector and/or screen?" [:current-proposal :can-provide-projector?] state-atom]]]])))


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
         [schema-row "Desired number of performances" "No guarantees." [:current-proposal :inside-performances] state-atom]
         [:p "For non-traditional (and possibly traditional) spaces..."]
         [schema-textarea-row "Describe your space needs." "Give real measurements if you can or reference local stages." [:current-proposal :space-needs] state-atom]
         [schema-textarea-row "What is the minimum amount of space for your performance." "Can we cram you into a small venue?" [:current-proposal :space-needs-minimum] state-atom]
         [schema-boolean-row "Do you need a power outlet?" [:current-proposal :need-power?] state-atom]
         [schema-boolean-row "Does your act require a sound system?" [:current-proposal :basic-sound-system?] state-atom] ;"A few select venues can provide them."
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


(defn sign-agreement? []
  (.log js/console (str "sign: " (map
                                   (fn [q]

                                     (session/get-in [:current-agreement q]))
                                   (keys (session/get-in [:current-agreement])))))
  (not
    (some #(= false %)
          (map
            (fn [q]
              (session/get-in [:current-agreement q]))
            (keys (session/get-in [:current-agreement]))))))


(defn proposal-agreement []
  (let [visible? (r/atom (session/get-in [:current-proposal :signed-agreement?]))]
    (if (nil? (session/get-in [:current-proposal :signed-agreement?]))
      (session/assoc-in! [:current-agreement] {:q0 true :q1 false :q2 false :q3 false :q4 false :q5 false :q6 false :q7 false :q8 false :q9 false :q10 false
                                               :q11 false :q12 false :q13 false :q14 false :q15 false :q16 false :q17 false :q18 false :q19 false}))
    (.log js/console (nil? (session/get-in [:current-proposal :signed-agreement?])))

    (fn []
      [:div.panel.panel-default
       [:div.panel-heading
        [:h4 (str "You must agree to the following in order to submit.")]
        [control-row state-atom]
        ;[hints-pane]
        ]
       [:div.panel-body
        (if visible?
          [:div.col-md-12
           [:p "HEY! The Infringement Festival is an incredible experience. This festival is only possible with the help and dedication of the organizers and volunteers who help make Infringement happen."]
           [:p "That being said, we need your help. Unlike other festivals of this size, we have less than a dozen year round organizers that make Infringement happen. To get the full understanding of the festivals' rules and values, make sure to check out our official Mandate."]
           [:p "By agreeing to the conditions below, you will be allowed to partake in what is possibly the most independent festival in the world!"]
           [:p [:span {:style {:text-decoration "underline"}} "Your proposal submission is not complete until you have checked every box in this section."] " This proves to us you have read and understand these statements."]
           [:h4 "Communication"]
           [schema-checkbox-row "I agree to keep in touch with my genre organizer and venue contact. This information will be provided shortly after making a proposal." [:current-agreement :q1] state-atom]
           [schema-checkbox-row "I will contact my genre organizer and venue organizer as soon as possible if I need to cancel a performance." [:current-agreement :q2] state-atom]
           [schema-checkbox-row "I will check my e-mail and voicemail regularly, and respond to requests from my genre organizer as soon as possible." [:current-agreement :q3] state-atom]
           [schema-checkbox-row "I will add them to my \"accepted senders list\" so that the emails do not go into my spam folder. I will stay in contact with them, knowing that if I don't, my proposal will be deleted." [:current-agreement :q4] state-atom]
           [:h4 "Respect"]
           [schema-checkbox-row "I will conduct myself in a courteous and professional manner." [:current-agreement :q5] state-atom]
           [schema-checkbox-row "I will treat the venue with the utmost care and I will leave it in the same condition as when I arrived." [:current-agreement :q6] state-atom]
           [schema-checkbox-row "I understand that if I am scheduled into a group show with other acts, it is proper etiquette, time permitting, to arrive early and stay late to watch the other performers on the bill." [:current-agreement :q7] state-atom]
           [schema-checkbox-row "I understand that this is a \"DIY\" festival and that any and all promotion for my proposal must come from myself." [:current-agreement :q8] state-atom]
           [:h4 "Punctuality"]
           [schema-checkbox-row "I will attend at least one planning/informational meeting before the festival. (If I live outside of Western New York or my schedule does not allow me to attend the regular meeting, I will contact my genre organizer to schedule a phone call." [:current-agreement :q9] state-atom]
           [schema-checkbox-row "I understand that I must be at my scheduled venue at least 45 minutes before my scheduled performance time." [:current-agreement :q10] state-atom]
           [schema-checkbox-row "I understand I must be at my location early to start on time." [:current-agreement :q11] state-atom]
           [:h4 "Flexibility"]
           [schema-checkbox-row "I understand that I may have to be flexible with my schedule and my performances leading up to and during the festival. I understand that with a festival of this size, put together entirely by volunteers, there are bound to be some scheduling mishaps, etc. I may have to make the best of whatever happens during the festival." [:current-agreement :q12] state-atom]
           [:h4 "Money"]
           [schema-checkbox-row "I understand that certain sound expenses may occur at some venues, and money may be taken from the door to pay for these costs." [:current-agreement :q13] state-atom]
           [schema-checkbox-row "I understand that no performer is guaranteed financial compensation. Many shows are free, and the only way of compensation is pass the hat / donation." [:current-agreement :q14] state-atom]
           ;[schema-checkbox-row "I understand that if I am selected to play with an out of town act during a group show, I may be asked to donate portions of my share from the door to the out of town act." [:current-agreement :q15] state-atom]
           [:h4 "Street Performances"]
           [schema-checkbox-row "I understand that weather is always a factor and it is my responsibility to avoid dangerous conditions." [:current-agreement :q15] state-atom]
           [schema-checkbox-row "I understand that I must obtain a street-performer's permit to legally street-perform in Buffalo." [:current-agreement :q16] state-atom]
           [schema-checkbox-row "I understand that if I do anything illegal during my, the Infringement Festival cannot do anything if I am asked to stop by the Buffalo Police Department." [:current-agreement :q17] state-atom]
           [schema-checkbox-row "I understand that if busking, I will not perform at one location for more than 2 hours at a time and will share the space with other acts who would like to perform." [:current-agreement :q18] state-atom]
           [schema-checkbox-row "I will do my best to cooperate with the reasonable requests of store-owners and people in the neighborhood." [:current-agreement :q19] state-atom]])]
       [:div.panel-footer
        [:div.col-md-12
         [:div.row
          (.log js/console (str "current agreement: " (session/get-in [:current-agreement])))
          (.log js/console (str "signable?: " (sign-agreement?)))
          (if (session/get-in [:current-proposal :signed-agreement?])
            [:p "You have agreed to our demands."]

            (if (true? (sign-agreement?))
              [:input {:type "button"
                       :value "I agree to the above terms."
                       :on-click #(session/swap! assoc-in [:current-proposal :signed-agreement?] true)}]
              [:p "Check the above boxes in order to finalize your agreement."]))]]]])))

(defn proposals-page []
  ;(get-logged-in-user-proposals-from-server)
  (fn []
    (let [user (session/get :user)]
      (set-title! "Proposals")
      (.log js/console (str "Proposals: " (session/get-in [:proposals])))
      (.log js/console (str "Current Proposal: " (session/get-in [:current-proposal])))
      (if
        (or (nil? user) (nil? (session/get-in [:proposals])))
        [:div.row
         [:div.col-md-12
          [:p "You have no proposals."]]]
        [:div.row
         [:div.col-md-12
          [:div.row
           [:h2 "Proposals"]
           [:p (str "Logged in: " (:username user))]]
          [:div.row
            [:p (str "Overall status: proposal incomplete")]]
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
          [proposal-agreement]]]))))

