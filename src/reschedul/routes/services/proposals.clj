(ns reschedul.routes.services.proposals
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]
            [reschedul.routes.services.users :refer [User]]))

(s/defschema Availability {(s/optional-key :thu0)  s/Str
                           (s/optional-key :fri0)  s/Str
                           (s/optional-key :sat0)  s/Str
                           (s/optional-key :sun0)  s/Str
                           (s/optional-key :mon0)  s/Str
                           (s/optional-key :tue0)  s/Str
                           (s/optional-key :wed0)  s/Str
                           (s/optional-key :thu1)  s/Str
                           (s/optional-key :fri1)  s/Str
                           (s/optional-key :sat1)  s/Str
                           (s/optional-key :sun1)  s/Str
                           (s/optional-key :mon1)  s/Str
                           (s/optional-key :tue1)  s/Str
                           (s/optional-key :wed1)  s/Str
                           (s/optional-key :thu2)  s/Str
                           (s/optional-key :fri2)  s/Str
                           (s/optional-key :sat2)  s/Str
                           (s/optional-key :sun2)  s/Str
                           (s/optional-key :mon2)  s/Str
                           (s/optional-key :notes) s/Str})

(s/defschema PromotionalInfo {(s/optional-key :label)              s/Str
                              (s/optional-key :production-company) s/Str
                              (s/optional-key :affiliations)       s/Str
                              (s/optional-key :website)            s/Str
                              (s/optional-key :facebook-link)      s/Str
                              (s/optional-key :facebook-handle)    s/Str
                              (s/optional-key :twitter-link)       s/Str
                              (s/optional-key :twitter-handle)     s/Str
                              (s/optional-key :soundcloud-link)    s/Str
                              (s/optional-key :vimeo-link)         s/Str
                              (s/optional-key :youtube-link)       s/Str
                              (s/optional-key :reverbnation-link)  s/Str
                              (s/optional-key :bandcamp-link)      s/Str
                              (s/optional-key :notes)              s/Str})


(s/defschema PerformanceProposal {:_id                                     s/Str
                                  :title                                   s/Str
                                  :genre                                   (s/enum "music" "dance" "film" "spokenword" "theater" "visualart")
                                  :proposer                                s/Str
                                  :state                                   s/Str
                                  (s/optional-key :availability)           Availability
                                  (s/optional-key :promotional-info)       PromotionalInfo
                                  (s/optional-key :assigned-genre)         s/Str

                                  (s/optional-key :assigned-organizer)     User

                                  (s/optional-key :number-of-performers)   s/Bool
                                  (s/optional-key :performers-names)       s/Str

                                  (s/optional-key :description-private)    s/Str
                                  (s/optional-key :description-public)     s/Str
                                  (s/optional-key :description-public-140) s/Str
                                  (s/optional-key :notes)                  s/Str

                                  (s/optional-key :setup-time) s/Str
                                  (s/optional-key :run-time) s/Str
                                  (s/optional-key :teardown-time) s/Str
                                  (s/optional-key :rating) s/Str
                                  (s/optional-key :twentyone?) s/Str
                                  (s/optional-key :seating?) s/Str
                                  (s/optional-key :projection-self) s/Str
                                  (s/optional-key :projection-other) s/Str

                                  (s/optional-key :space-prearranged) s/Str
                                  (s/optional-key :share-space?) s/Bool
                                  (s/optional-key :space-needs) s/Str
                                  (s/optional-key :power-needs) s/Str
                                  (s/optional-key :amp-needs) s/Str
                                  (s/optional-key :basic-sound-system?) s/Bool
                                  (s/optional-key :seating-needed?) s/Bool
                                  (s/optional-key :gear-to-share) s/Str

                                  (s/optional-key :drums-backline-to-provide) s/Str
                                  (s/optional-key :full-sound-system?) s/Bool
                                  (s/optional-key :how-loud) s/Int

                                  (s/optional-key :live-performance?) s/Bool
                                  (s/optional-key :installation?) s/Bool
                                  (s/optional-key :film-genre) s/Str
                                  (s/optional-key :film-duration) s/Int
                                  (s/optional-key :preview-urls) s/Str
                                  (s/optional-key :can-facilitate-screening) s/Str
                                  (s/optional-key :can-provide-projector) s/Str

                                  (s/optional-key :number-of-pieces) s/Int
                                  (s/optional-key :pieces-list) s/Str
                                  (s/optional-key :prearranged-gallery) s/Str})



(defroutes* proposal-secure-routes
            (context* "" []
                      :tags ["proposals"]
                      ;(GET* "/proposals/stats" []
                      ;      :return UserStats
                      ;      :summary "Just some data to demo a public route."
                      ;      (ok (db/get-proposals-stats)))
                      (GET* "/proposals" []
                            :return [PerformanceProposal]
                            :summary "All proposals data"
                            (ok (db/stringify_ids (db/get-all-proposals))))
                      (GET* "/proposals/:id" []
                            :path-params [id :- String]
                            :return PerformanceProposal
                            :summary "Proposal and its data"
                            (ok (db/stringify_id (db/get-proposal-by-id id))))
                      (GET* "/proposals/title/:title" []
                            :path-params [title :- String]
                            :return PerformanceProposal
                            :summary "Proposal and its data"
                            (ok (db/stringify_id (db/get-proposal-by-title title))))
                      (GET* "/proposals/genre/:genre" []
                            :path-params [genre :- String]
                            :return [PerformanceProposal]
                            :summary "All proposals for a genre"
                            (ok (db/stringify_ids (db/get-proposals-for-genre genre))))

                      (GET* "/proposals/user/:username" []
                            :path-params [username :- String]
                            :return [PerformanceProposal]
                            :summary "All proposals for a user"
                            (ok (db/stringify_ids (db/get-proposals-for-user username))))

                      (POST* "/proposals" []
                             :return PerformanceProposal
                             :body [proposal (describe PerformanceProposal "new proposal")]
                             :summary "new proposal, baby, yeah!"
                             (ok (db/stringify_id (db/proposal-create! proposal))))
                      ; + update the record
                      (POST* "/proposals/:id" []
                             :path-params [id :- String]
                             :return PerformanceProposal
                             :body [proposal (describe PerformanceProposal "update proposal")]
                             :summary "update proposal info"
                             (ok (db/stringify_id (db/proposal-update! proposal))))
                      (POST* "/proposals/:id/delete" []
                             :return PerformanceProposal
                             :body [id :- String]
                             :summary "Delete the proposal"
                             (ok (db/stringify_id (db/proposal-delete! id))))))
