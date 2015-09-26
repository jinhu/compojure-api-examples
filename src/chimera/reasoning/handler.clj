(ns chimera.reasoning.handler
    (:require [compojure.api.sweet :refer :all]
      [ring.util.http-response :refer :all]
              [chimera.reasoning.example :refer :all]
              [chimera.reasoning.measurement :refer :all]
              [chimera.reasoning.device :refer :all]

      [schema.core :as s]))


(defapi app
        (swagger-ui)
        (swagger-docs)
        measurement-routes
        device-routes
        example-routes
        )
