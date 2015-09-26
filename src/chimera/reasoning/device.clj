(ns chimera.reasoning.device
  (:require [schema.core :as s]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.swagger.schema :refer [coerce!]]
            )
  )
(s/defschema Device {:id        Long
                     :timestamp Long
                     :type      String
                     :device_id Long
                     :value     Long})

(s/defschema NewDevice (dissoc Device :id))


(defonce device-id-seq (atom 0))
(defonce devices (atom (array-map)))

(defn get-device [id] (@devices id))
(defn list-devices [] (-> devices deref vals reverse))
(defn delete-device! [id] (swap! devices dissoc id) nil)

(defn create-device! [new-device]
  (let [id (swap! device-id-seq inc)
        device (coerce! Device (assoc new-device :id id))]
    (swap! devices assoc id device)
    device))

(defn update-device! [device]
  (let [device (coerce! Device device)]
    (swap! devices assoc (:id device) device)
    (get-device (:id device))))

;; Data

(when (empty? @devices)
  (create-device! {:type "temperature" :value 950 :timestamp 1 :device_id 2})
  (create-device! {:type "temperature" :value 12 :timestamp 1 :device_id 2}))

(defroutes*
  device-routes
  (context*
    "/devices" []
    :tags ["device"]
    (GET* "/" [] :return [Device] (ok (list-devices)))
    (POST* "/" [] :return Device :body [device NewDevice] (ok (create-device! device)))
    (PUT* "/" [] :return Device :body [device Device] (ok (update-device! device)))
    (GET* "/:id" [] :return Device :path-params [id :- Long] (ok (get-device id)))
    (DELETE* "/:id" [] :path-params [id :- Long] (ok (delete-device! id)))))
