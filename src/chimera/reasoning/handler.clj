(ns chimera.reasoning.handler
    (:require
      [compojure.api.sweet :refer :all]
      ;[compojure.core :refer :all]
      [ring.util.http-response :refer :all]
      [chimera.reasoning.example :refer :all]
      [chimera.reasoning.measurement :refer :all]
      [chimera.reasoning.user :refer :all]
      [chimera.reasoning.device :refer :all]
      [chimera.reasoning.situation :refer :all]

      [schema.core :as s]))

(defapi app
        (swagger-ui)
        (swagger-docs)
        measurement-routes
        device-routes
        example-routes
        user-routes
        situation-routes

        )

