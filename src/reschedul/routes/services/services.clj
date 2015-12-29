(ns reschedul.routes.services.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [buddy.auth.accessrules :refer [restrict]]
            [reschedul.routes.services.auth :as auth]
            [reschedul.routes.services.users :as users]
            [reschedul.routes.services.venues :as venues]
            [reschedul.routes.services.proposals :as proposals]))

(defn on-error
  [request value]
  {:status 403
   :headers {}
   :body { :error "Not authorized"}})

(defapi service-routes
        (ring.swagger.ui/swagger-ui
          "/swagger-ui")
        ;JSON docs available at the /swagger.json route
        (swagger-docs
          {:info {:title "Reschedul api"}})
        (context* "/api" []
                  :tags [:api]
                  auth/auth-routes
                  (restrict users/user-secure-routes {:handler auth/is-authenticated?
                                               :on-error on-error})
                  (restrict venues/venue-secure-routes {:handler auth/is-authenticated?
                                                 :on-error on-error})))
                  ;;(restrict proposals/proposal-routes {:handler auth/is-authenticated?})


; next up
;proposals/proposal-routes
;tags/tag-routes

; ???
;messages/message-routes