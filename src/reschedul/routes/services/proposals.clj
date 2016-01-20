(ns reschedul.routes.services.proposals
  (:require [ring.util.http-response :refer [ok]]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]
            ;[reschedul.routes.services.proposal_info :as proposal-info]
            ))


(s/defschema PromotionalInfo {;:_id                                 s/Str
                              (s/optional-key :label)              s/Str
                              (s/optional-key :production-company) s/Str
                              (s/optional-key :affiliations)       s/Str
                              (s/optional-key :website)            s/Str
                              (s/optional-key :facebook-link)      s/Str
                              (s/optional-key :twitter-link)       s/Str
                              (s/optional-key :media-link)         s/Str
                              (s/optional-key :promo-notes)        s/Str})
;


(s/defschema PerformanceProposal {:_id                                        s/Str
                                  :title                                      s/Str
                                  :category                                   s/Str
                                  (s/optional-key :genre-tags)                s/Str
                                  :proposer-username                          s/Str
                                  :assigned-organizer-username                s/Str

                                  ;(s/optional-key :proposal-info-id) s/Str
                                  (s/optional-key :availability-info-id)      s/Str
                                  (s/optional-key :promotional-info-id)       s/Str

                                  (s/optional-key :primary-contact-name)      s/Str
                                  (s/optional-key :primary-contact-email)     s/Str
                                  (s/optional-key :primary-contact-phone)     s/Str
                                  (s/optional-key :primary-contact-role)      s/Str

                                  (s/optional-key :secondary-contact-name)    s/Str
                                  (s/optional-key :secondary-contact-email)   s/Str
                                  (s/optional-key :secondary-contact-phone)   s/Str
                                  (s/optional-key :secondary-contact-role)    s/Str

                                  (s/optional-key :number-of-performers)      s/Int
                                  (s/optional-key :performers-names)          s/Str
                                  (s/optional-key :potential-conflicts)       s/Str

                                  (s/optional-key :description-private)       s/Str
                                  (s/optional-key :description-public)        s/Str
                                  (s/optional-key :description-public-140)    s/Str
                                  (s/optional-key :general-notes)             s/Str

                                  (s/optional-key :setup-time)                s/Str
                                  (s/optional-key :run-time)                  s/Str
                                  (s/optional-key :teardown-time)             s/Str
                                  (s/optional-key :rating)                    s/Str
                                  (s/optional-key :twentyone?)                s/Bool
                                  (s/optional-key :seating?)                  s/Bool
                                  (s/optional-key :projection-self?)          s/Bool
                                  (s/optional-key :projection-other?)         s/Bool
                                  (s/optional-key :space-prearranged?)        s/Bool
                                  (s/optional-key :share-space?)              s/Bool
                                  (s/optional-key :opening-ceremonies?)       s/Bool
                                  (s/optional-key :group-proposal-ideas)      s/Str
                                  (s/optional-key :venues-not-perform)        s/Str
                                  (s/optional-key :inside-number-of-performances) s/Str

                                  (s/optional-key :space-needs)               s/Str
                                  (s/optional-key :space-needs-minimum)       s/Str
                                  (s/optional-key :power-needs)               s/Str
                                  (s/optional-key :amp-needs)                 s/Str
                                  (s/optional-key :basic-sound-system?)       s/Bool
                                  (s/optional-key :seating-needed?)           s/Bool
                                  (s/optional-key :gear-to-share)             s/Str
                                  (s/optional-key :setup-notes)               s/Str

                                  (s/optional-key :drums-backline-to-provide) s/Str
                                  (s/optional-key :full-sound-system?)        s/Bool
                                  (s/optional-key :how-loud)                  s/Int

                                  (s/optional-key :live-performance?)         s/Bool
                                  (s/optional-key :installation?)             s/Bool
                                  (s/optional-key :film-genre)                s/Str
                                  (s/optional-key :film-duration)             s/Int
                                  (s/optional-key :preview-urls)              s/Str
                                  (s/optional-key :can-facilitate-screening)  s/Str
                                  (s/optional-key :can-provide-projector)     s/Str

                                  (s/optional-key :pieces-list)               s/Str
                                  (s/optional-key :gallery-prearranged)       s/Str

                                  (s/optional-key :outide-outide-busk-perform-preference) s/Bool
                                  (s/optional-key :outside-license?)          s/Bool
                                  (s/optional-key :outside-experience?)       s/Bool
                                  (s/optional-key :outside--number-of-performances) s/Str
                                  (s/optional-key :outside-roam?)             s/Bool
                                  (s/optional-key :outside-interaction?)      s/Bool
                                  (s/optional-key :outside-battery?)          s/Bool })


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
                            :return PerformanceProposal
                            :summary "Proposal and its data"
                            (ok (db/stringify_id (db/get-proposal-by-id id))))

                      ; "BATCH" gets ---there should be a couple ?
                      (GET* "/category/:category" []
                            :path-params [category :- String]
                            :return [PerformanceProposal]
                            :summary "All proposals for a category"
                            (ok (db/stringify_ids (db/get-proposals-for-category category))))

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
