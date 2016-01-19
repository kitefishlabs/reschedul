(ns reschedul.routes.services.venues
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]))


(s/defschema VenueSummary {:_id                            s/Str
                           :name                           s/Str
                           ;(s/optional-key :latitude)      s/Str
                           ;(s/optional-key :longitude)     s/Str
                           })

(s/defschema Venue {:_id                                   s/Str
                    :name                                  s/Str
                    :active                                s/Bool

                    ;:availability                          Availability

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
                    (s/optional-key :phone)                s/Str
                    (s/optional-key :notes)                s/Str})


(defroutes* venue-secure-routes
  (context* "/venue" []
            :tags ["venue"]

            (GET* "/" []
                  :return [Venue]
                  :summary "All venues and their data"
                  (ok (db/stringify_ids (db/venues-all))))
            (GET* "/names" []
                  :return [VenueSummary]
                  :summary "All ids and names."
                  (ok (db/stringify_ids (db/venues-all-ids-names))))
            (GET* "/:pg/:per" []
                  :return [Venue]
                  :path-params [pg :- String, per :- String]
                  :summary "All venues and their data, paginated"
                  (ok (db/stringify_ids (db/venues-all-pag pg per))))

            (GET* "/:id" []
                  :return Venue
                  :path-params [id :- String]
                  :summary "Single venue and its data"
                  (ok (db/stringify_id (db/find-venue-by-id id))))

            ; Individual commands that include db writes
            ; + create new
            (POST* "/" []
                   :return Venue
                   :body [venue (describe Venue "new venue")]
                   :summary "venue, baby, yeah!"
                   (ok (db/stringify_id (db/venue-create! venue))))
            ;; + update the record
            (POST* "/:id" []
                   :path-params [id :- String]
                   :return Venue
                   :body [venue (describe Venue "updating venue")]
                   :summary "venue, baby, yeah!"
                   (ok (db/stringify_id (db/venue-update! venue))))

            ; Dev test
            ;(POST* "/venueunev" []
            ;       :return Venue
            ;       :body [ven (describe Venue "new ven")]
            ;       :summary "ven, yeah!"
            ;       (ok ven))

            ; DEACTIVATE, NOT DELETE
            ; - there will be an admin-only function to perform actual deletes
            ;(POST* "/venue/:id/activate" []
            ;       :return Venue
            ;       :body [venue (describe Venue "deactivate the venue")]
            ;       :summary "Mark a venue as deleted. AKA inactive!"
            ;       (ok (db/)))))

            ))