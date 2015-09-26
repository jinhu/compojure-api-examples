(ns chimera.reasoning.measurement
    (:require [schema.core :as s]
      [compojure.api.sweet :refer :all]
      [ring.util.http-response :refer :all]
      ;[compojure.core :refer :all]
      ;[compojure.api.core :refer :all]
      ;[ring.util.http-response :refer :all]
      [ring.swagger.schema :refer [coerce!]]
      )
    )

;; Domain

(def Topping (s/enum :cheese :olives :ham :pepperoni :habanero))

(s/defschema Measurement {:id        Long
                          :timestamp Long
                          :type      String
                          :device_id Long
                          :value     Long})

(s/defschema NewMeasurement (dissoc Measurement :id))


(defonce id-seq (atom 0))
(defonce measurements (atom (array-map)))

(defn get-measurement [id] (@measurements id))
(defn list-measurements [] (-> measurements deref vals reverse))
(defn delete-measurement! [id] (swap! measurements dissoc id) nil)

(defn create-measurement! [new-measurement]
      (let [id (swap! id-seq inc)
            measurement (coerce! Measurement (assoc new-measurement :id id))]
           (swap! measurements assoc id measurement)
           measurement))

(defn update-measurement! [measurement]
      (let [measurement (coerce! Measurement measurement)]
           (swap! measurements assoc (:id measurement) measurement)
           (get (:id measurement))))

;; Data

(when (empty? @measurements)
      (create-measurement! {:type "temperature" :value 950 :timestamp 1 :device_id 2})
      (create-measurement! {:type "temperature" :value 12 :timestamp 1 :device_id 2}))

(defroutes* measurement-routes
            (context* "/measurements" []
                      :tags ["measurement"]
                      (GET* "/" [] :return [Measurement] (ok (list-measurements)))
                      (POST* "/" [] :return Measurement :body [measurement NewMeasurement] (ok (create-measurement! measurement)))
                      (PUT* "/" [] :return Measurement :body [measurement Measurement] (ok (update-measurement! measurement)))
                      (GET* "/:id" [] :return Measurement :path-params [id :- Long] (ok (get-measurement id)))
                      (DELETE* "/:id" [] :path-params [id :- Long] (ok (delete-measurement! id)))))
