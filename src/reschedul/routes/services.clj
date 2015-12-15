(ns reschedul.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]))

(s/defschema VenueSummary {
                           :_id                            s/Str
                           :name                           s/Str
                           :active                         s/Bool
                           (s/optional-key :short_name)    s/Str})

(s/defschema VenueSummaryLoc {
                              :_id                            s/Str
                              :name                           s/Str
                              :active                         s/Bool
                              (s/optional-key :latitude)      s/Str
                              (s/optional-key :longitude)     s/Str
                              (s/optional-key :short_name)    s/Str})
(s/defschema Venue {
                    :_id                                   s/Str
                    :name                                  s/Str
                    :active                                s/Bool

                    (s/optional-key :short_name)           s/Str
                    (s/optional-key :venue_type)           s/Str
                    (s/optional-key :address)              s/Str

                    (s/optional-key :description)          s/Str
                    (s/optional-key :description_for_web)  s/Str

                    (s/optional-key :latitude)             s/Str
                    (s/optional-key :longitude)            s/Str

                    (s/optional-key :owner)                s/Str
                    (s/optional-key :contact)              s/Str
                    (s/optional-key :infringement_contact) s/Str
                    (s/optional-key :contact_phone)        s/Str
                    (s/optional-key :contact_e-mail)       s/Str
                    (s/optional-key :website)              s/Str
                    (s/optional-key :phone)                s/Str})

(s/defschema NewVenue (dissoc Venue :_id))


(defapi service-routes
  (ring.swagger.ui/swagger-ui
   "/swagger-ui")
  ;JSON docs available at the /swagger.json route
  (swagger-docs
    {:info {:title "Sample api"}})
  (context* "/api" []
            :tags ["venues"]

            (GET* "/venue" []
                  :return Venue
                  :summary "A venues and its data"
                  (ok (db/stringify_id (db/venues-one-example)))) ; TODO : merge transform_id(s) functions
            (GET* "/venues" []
                  :return [Venue]
                  :summary "All venues and their data"
                  (ok (db/stringify_ids (db/venues-all))))
            (GET* "/venues/info" []
                  :return [VenueSummary]
                  :summary "All venues and summaries of their data"
                  (ok (db/stringify_ids (db/venues-all-ids-names false))))
            (GET* "/venues/location" []
                  :return [VenueSummaryLoc]
                  :summary "All venues and summaries of their data"
                  (ok (db/stringify_ids (db/venues-all-ids-names true))))
            (GET* "/venues/:pg/:per" []
                  :return [Venue]
                  :path-params [pg :- String, per :- String]
                  :summary "All venues and their data, paginated"
                  (ok (db/stringify_ids  (db/venues-all-pag pg per))))

            (GET* "/venue/:id" []
                  :return Venue
                  :path-params [id :- String]
                  :summary "Single venue and its data"
                  (ok (db/stringify_id  (db/find-venue-by-id id))))


            ; Individual commands that include db writes

            ; + create new
            (POST* "/venue" []
                   :return Venue
                   :body [venue (describe Venue "new venue")]
                   :summary "venue, baby, yeah!"
                   (ok (db/stringify_id  (db/venue-create! venue))))
            ; + update the record
            (POST* "/venue/:id" []
                   :path-params [id :- String]
                   :return Venue
                   :body [venue (describe Venue "updating venue")]
                   :summary "venue, baby, yeah!"
                   (ok (db/stringify_id  (db/venue-update! venue))))))

            ; Dev test
            ;(POST* "/venueunev" []
            ;       :return Ven
            ;       :body [ven (describe Ven "new ven")]
            ;       :summary "ven, yeah!"
            ;       (ok ven))

            ; DEACTIVATE, NOT DELETE
            ; - there will be an admin function to perform actual deletes
            ;(POST* "/venue/:id/activate" []
            ;       :return Venue
            ;       :body [venue (describe Venue "deactivate the venue")]
            ;       :summary "Mark a venue as deleted. AKA inactive!"
            ;       (ok (db/)))))

