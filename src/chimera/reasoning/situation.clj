(ns chimera.reasoning.situation
    (:require
      [schema.core :as s]
      [compojure.api.sweet :refer :all]
      [ring.util.http-response :refer :all]
      [ring.swagger.schema :refer [coerce!]]
      ;[schema.experimental.abstract-map :as abstract-map]

      )
    )



(def LightStatus (s/enum :on :off))
(def CatLocation (s/enum :outside :inside))

(s/defschema Subject
             ;             (abstract-map/abstract-map-schema
               :type
               {:id   Long
                :name s/Str})
;)




(abstract-map/extend-schema Situation Subject
                            [:situation] {
                                          :light? LightStatus
                                          :night? s/Bool
                                          :cat    CatLocation})
(s/defschema NewSituation (dissoc Situation :id))

(abstract-map/extend-schema Cat Subject [:cat] {:claws? s/Bool})
(abstract-map/extend-schema Dog Subject [:dog] {:barks? s/Bool})

(s/validate Cat {:type :cat :name "melvin" :claws? true})
(s/validate Subject {:type :cat :name "melvin" :claws? true})
(s/validate Subject {:type :dog :name "roofer" :barks? true})
(s/validate Subject {:type :cat :name "confused kitty" :barks? true})

;; RuntimeException: Value does not match schema: {:claws? missing-required-key, :barks? disallowed-key}
(defonce situation-id-seq (atom 0))
(defonce situations (atom (array-map)))

(defn get-situation [id] (@situations id))
(defn list-situations [] (-> situations deref vals reverse))
(defn delete-situation! [id] (swap! situations dissoc id) nil)

(defn create-situation! [new-situation]
      (let [id (swap! situation-id-seq inc)
            situation (coerce! Situation (assoc new-situation :id id))]
           (swap! situations assoc id situation)
           situation))

(defn update-situation! [situation]
      (let [situation (coerce! Situation situation)]
           (swap! situations assoc (:id situation) situation)
           (get-situation (:id situation))))

;; Data

(when (empty? @situations)
      (create-situation! {:type "temperature" :value 950 :timestamp 1 :situation_id 2})
      (create-situation! {:type "temperature" :value 12 :timestamp 1 :situation_id 2}))

(defroutes*
  situation-routes
  (context*
    "/situations" []
    :tags ["situation"]
    (GET* "/" [] :return [Situation] (ok (list-situations)))
    (POST* "/" [] :return Situation :body [situation NewSituation] (ok (create-situation! situation)))
    (PUT* "/" [] :return Situation :body [situation Situation] (ok (update-situation! situation)))
    (GET* "/:id" [] :return Situation :path-params [id :- Long] (ok (get-situation id)))
    (DELETE* "/:id" [] :path-params [id :- Long] (ok (delete-situation! id)))))
