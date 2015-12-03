(ns reschedul.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.db.core :as db]))

(s/defschema Venue {
                    :_id                                   String
                    :name                                  String
                    (s/optional-key :short_name)           String
                    (s/optional-key :venue_type)           String
                    (s/optional-key :address)              String

                    (s/optional-key :description)          String
                    (s/optional-key :description_for_web)  String

                    (s/optional-key :latitude)             String
                    (s/optional-key :longitude)            String

                    (s/optional-key :owner)                String
                    (s/optional-key :contact)              String
                    (s/optional-key :infringement_contact) String
                    (s/optional-key :contact_phone)        String
                    (s/optional-key :contact_e-mail)       String
                    (s/optional-key :website)              String
                    (s/optional-key :phone)                String})

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
                  (ok (db/transform_id (db/venues-one))))
            (GET* "/venues" []
                  :return [Venue]
                  :summary "All venues and their data"
                  (ok (db/transform_ids (db/venues-all))))
            (GET* "/venues/:pg/:per" []
                  :return [Venue]
                  :path-params [pg :- String, per :- String]
                  :summary "All venues and their data, paginated"
                  (ok (db/transform_ids (db/venues-all-pag pg per))))
            (GET* "/venue/:id" []
                  :return Venue
                  :path-params [id :- String]
                  :summary "Single venue and its data"
                  (ok (db/transform_id (db/find-venue-by-id id))))))
