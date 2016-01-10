(ns reschedul.routes.services.proposals
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]
            ;[reschedul.routes.services.users :refer [User]]
            ))

;(s/defschema Availability {(s/optional-key :thu1)  s/Str
;                           (s/optional-key :fri1)  s/Str
;                           (s/optional-key :sat1)  s/Str
;                           (s/optional-key :sun1)  s/Str
;                           (s/optional-key :mon1)  s/Str
;                           (s/optional-key :tue1)  s/Str
;                           (s/optional-key :wed1)  s/Str
;                           (s/optional-key :thu2)  s/Str
;                           (s/optional-key :fri2)  s/Str
;                           (s/optional-key :sat2)  s/Str
;                           (s/optional-key :sun2)  s/Str
;                           (s/optional-key :availability-notes) s/Str})
;
;(s/defschema PromotionalInfo {(s/optional-key :label)              s/Str
;                              (s/optional-key :production-company) s/Str
;                              (s/optional-key :affiliations)       s/Str
;                              (s/optional-key :website)            s/Str
;                              (s/optional-key :facebook-link)      s/Str
;                              (s/optional-key :twitter-link)       s/Str
;                              (s/optional-key :soundcloud-link)    s/Str
;                              (s/optional-key :vimeo-link)         s/Str
;                              (s/optional-key :youtube-link)       s/Str
;                              (s/optional-key :reverbnation-link)  s/Str
;                              (s/optional-key :bandcamp-link)      s/Str
;                              (s/optional-key :promo-notes)        s/Str})
;
;
(s/defschema PerformanceProposal {:_id                                             s/Str
                                  :title                                           s/Str
                                  :genre                                           (s/enum "music" "dance" "film" "spokenword" "theater" "visualart" "none")
                                  (s/optional-key :genre-tags)                     s/Str
                                  :proposer                                        s/Str
                                  :state                                           s/Str

                                  ;(s/optional-key :availability)                   Availability
                                  ;(s/optional-key :promotional-info)               PromotionalInfo

                                  ;(s/optional-key :primary-contact-name)           s/Str
                                  ;(s/optional-key :primary-contact-email)          s/Str
                                  ;(s/optional-key :primary-contact-phone)          s/Str
                                  ;(s/optional-key :primary-contact-relationship)   s/Str
                                  ;
                                  ;(s/optional-key :secondary-contact-name)         s/Str
                                  ;(s/optional-key :secondary-contact-email)        s/Str
                                  ;(s/optional-key :secondary-contact-phone)        s/Str
                                  ;(s/optional-key :secondary-contact-relationship) s/Str
                                  ;
                                  ;
                                  ;(s/optional-key :assigned-genre)                 s/Str
                                  ;(s/optional-key :assigned-organizer)             User
                                  ;
                                  ;(s/optional-key :number-of-performers)           s/Int
                                  ;(s/optional-key :performers-names)               s/Str
                                  ;(s/optional-key :potential-conflicts)            s/Str
                                  ;
                                  ;(s/optional-key :description-private)            s/Str
                                  ;(s/optional-key :description-public)             s/Str
                                  ;(s/optional-key :description-public-140)         s/Str
                                  ;(s/optional-key :general-notes)                  s/Str
                                  })
;                                  (s/optional-key :setup-time) s/Str
;                                  (s/optional-key :run-time) s/Str
;                                  (s/optional-key :teardown-time) s/Str
;                                  (s/optional-key :rating) s/Str
;                                  (s/optional-key :twentyone?) s/Str
;                                  (s/optional-key :seating?) s/Str
;                                  (s/optional-key :projection-self) s/Str
;                                  (s/optional-key :projection-other) s/Str
;
;                                  (s/optional-key :space-prearranged) s/Str
;                                  (s/optional-key :share-space?) s/Bool
;                                  (s/optional-key :space-needs) s/Str
;                                  (s/optional-key :power-needs) s/Str
;                                  (s/optional-key :amp-needs) s/Str
;                                  (s/optional-key :basic-sound-system?) s/Bool
;                                  (s/optional-key :seating-needed?) s/Bool
;                                  (s/optional-key :gear-to-share) s/Str
;                                  (s/optional-key :setup-notes) s/Str
;                                  (s/optional-key :tech-notes) s/Str
;
;                                  (s/optional-key :drums-backline-to-provide) s/Str
;                                  (s/optional-key :full-sound-system?) s/Bool
;                                  (s/optional-key :how-loud) s/Int
;
;                                  (s/optional-key :live-performance?) s/Bool
;                                  (s/optional-key :installation?) s/Bool
;                                  (s/optional-key :film-genre) s/Str
;                                  (s/optional-key :film-duration) s/Int
;                                  (s/optional-key :preview-urls) s/Str
;                                  (s/optional-key :can-facilitate-screening) s/Str
;                                  (s/optional-key :can-provide-projector) s/Str
;
;                                  (s/optional-key :number-of-pieces) s/Int
;                                  (s/optional-key :pieces-list) s/Str
;                                  (s/optional-key :prearranged-gallery) s/Str})
;
;
;
(defroutes* proposal-secure-routes
            (context* "/proposal" []
                      :tags ["proposal"]
                      ;(GET* "/stats" []
                      ;      :return UserStats
                      ;      :summary "Just some data to demo a public route."
                      ;      (ok (db/get-proposals-stats)))
                      (GET* "/" []
                            :return [PerformanceProposal]
                            :summary "All proposals data"
                            (ok (db/stringify_ids (db/get-all-proposals))))
                      (GET* "/:id" []
                            :path-params [id :- String]
                            :return [PerformanceProposal]
                            :summary "Proposal and its data"
                            (ok (db/stringify_ids (db/get-proposal-by-id id))))

                      ; "BATCH" gets ---there should be a couple ?
                      (GET* "/genre/:genre" []
                            :path-params [genre :- String]
                            :return [PerformanceProposal]
                            :summary "All proposals for a genre"
                            (ok (db/stringify_ids (db/get-proposals-for-genre genre))))

                      (GET* "/user/:username" []
                            :path-params [username :- String]
                            :return [PerformanceProposal]
                            :summary "All proposals for a user"
                            (ok (db/stringify_ids (db/get-proposals-for-user username))))

                      ; CREATE AND UPDATE
                      (POST* "/" []
                             :return PerformanceProposal
                             :body [proposal PerformanceProposal {:description "A new proposal."}]
                             :summary "Adds proposal"
                             (ok (db/stringify_id (db/proposal-create! proposal))))
                      ; + update the record
                      (POST* "/:id" []
                             :path-params [id :- String]
                             :return PerformanceProposal
                             :body [proposal (describe PerformanceProposal "update proposal")]
                             :summary "update proposal info"
                             (ok (db/stringify_id (db/proposal-update! proposal))))
                      ; + DELETE
                      (POST* "/:id/delete" []
                             :return PerformanceProposal
                             :body [id :- String]
                             :summary "Delete the proposal"
                             (ok (db/stringify_id (db/proposal-delete! id))))))
