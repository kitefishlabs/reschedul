(ns reschedul.routes.services.proposals
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]
            [reschedul.routes.services.users :refer [ContactInfo SocialInfo]]
            [reschedul.routes.services.venues :refer [Availability]]))



(s/defschema PerformanceProposal {:_id                              s/Str
                                  :name                             s/Str
                                  :proposer-name                    s/Str
                                  :proposer-contact-info            ContactInfo
                                  :state                            s/Str
                                  :assigned-genre                   s/Str
                                  :assigned-organizer               s/Str
                                  :social-info                      SocialInfo
                                  :performers-availability          Availability
                                  (s/optional-key :performing-name) s/Str
                                  (s/optional-key :performers)      [s/Str]
                                  (s/optional-key :description)     s/Str
                                  (s/optional-key :description-160) s/Str

                                  (s/optional-key :tech-needs)      s/Str
                                  (s/optional-key :sound-needs)     s/Str
                                  (s/optional-key :video-needs)     s/Str
                                  (s/optional-key :floor-needs)     s/Str
                                  (s/optional-key :notes)           s/Str})

;(defroutes* proposal-routes
;            (GET* "/proposals" []
;                  :proposals [PerformanceProposal]
;                  :summary "All proposals and their data"
;                  (ok)) ;(db/stringify_ids (db/get-all-proposals))))
;            (GET* "/proposal/:id" []
;                  :path-params [id :- String]
;                  :return PerformanceProposal
;                  :summary "Performance and its data"
;                  (ok))) ;(db/stringify_ids (db/get-proposal id)))))
