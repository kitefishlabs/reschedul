(ns reschedul.routes.services.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul.routes.services.auth :as auth]
            [reschedul.routes.services.users :as users]
            [reschedul.routes.services.venues :as venues]
            [reschedul.routes.services.proposals :as proposals]))


(defapi service-routes
        (ring.swagger.ui/swagger-ui
          "/swagger-ui")
        ;JSON docs available at the /swagger.json route
        (swagger-docs
          {:info {:title "Sample api"}})
        (context* "/api" []
                  :tags [:users]
                  users/auth-routes
                  users/user-routes
                  venues/venue-routes))

; next up
;proposals/proposal-routes
;tags/tag-routes

; ???
;messages/message-routes