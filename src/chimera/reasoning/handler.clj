(ns chimera.reasoning.handler
  (:require
    [compojure.api.sweet :refer :all]
    ;[compojure.core :refer :all]
    [compojure.handler :as handler :refer [site]]
      [ring.util.http-response :refer :all]
              [chimera.reasoning.example :refer :all]
              [chimera.reasoning.measurement :refer :all]
    [cemerick.friend :as friend]
    [friend-oauth2.workflow :as oauth2]
    [friend-oauth2.util :refer [format-config-uri]]
    [chimera.reasoning.user :refer :all]
              [chimera.reasoning.device :refer :all]

      [schema.core :as s]))

(defapi app
        (swagger-ui)
        (swagger-docs)
        measurement-routes
        device-routes
        example-routes
        (handler/site
          (friend/authenticate
            user-routes
            {:allow-anon? true
             :workflows   [(oauth2/workflow
                             {:client-config client-config
                              :uri-config    uri-config
                              :credential-fn credential-fn})]}))
        )

