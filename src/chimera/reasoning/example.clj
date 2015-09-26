(ns chimera.reasoning.example
  (:require [schema.core :as s]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [ring.swagger.schema :refer [coerce!]]
            )
  )

(def Topping (s/enum :cheese :olives :ham :pepperoni :habanero))

(s/defschema Pizza {:id                           Long
                    :name                         String
                    :price                        Double
                    :hot                          Boolean
                    (s/optional-key :description) String
                    :toppings                     #{Topping}})

(s/defschema NewPizza (dissoc Pizza :id))

(s/defschema NewSingleToppingPizza (assoc NewPizza :toppings Topping))

;; Repository

(defonce id-seq (atom 0))
(defonce pizzas (atom (array-map)))

(defn get-pizza [id] (@pizzas id))
(defn get-pizzas [] (-> pizzas deref vals reverse))
(defn delete! [id] (swap! pizzas dissoc id) nil)

(defn add! [new-pizza]
  (let [id (swap! id-seq inc)
        pizza (coerce! Pizza (assoc new-pizza :id id))]
    (swap! pizzas assoc id pizza)
    pizza))

(defn update! [pizza]
  (let [pizza (coerce! Pizza pizza)]
    (swap! pizzas assoc (:id pizza) pizza)
    (get-pizza (:id pizza))))

;; Data

(when (empty? @pizzas)
  (add! {:name "Frutti" :price 9.50 :hot false :toppings #{:cheese :olives}})
  (add! {:name "Il Diablo" :price 12 :hot true :toppings #{:ham :habanero}}))
(s/defschema Total {:total Long})

(defroutes* example-routes
            (context* "/math" []
                      :tags ["math"]

                      (GET* "/plus" []
                            :return Total
                            :query-params [x :- Long, y :- Long]
                            :summary "x+y with query-parameters"
                            (ok {:total (+ x y)}))
                      (POST* "/minus" []
                             :return Total
                             :body-params [x :- Long, y :- Long]
                             :summary "x-y with body-parameters"
                             (ok {:total (- x y)}))
                      (GET* "/times/:x/:y" []
                            :return Total
                            :path-params [x :- Long, y :- Long]
                            :summary "x*y with path-parameters"
                            (ok {:total (* x y)}))
                      (GET* "/power" []
                            :return Total
                            :header-params [x :- Long, y :- Long]
                            :summary "x^y with header-parameters"
                            (ok {:total (long (Math/pow x y))})))

            (context* "/echo" []
                      :tags ["echo"]

                      (GET* "/request" req
                            (ok (dissoc req :body)))
                      (GET* "/pizza" []
                            :return NewSingleToppingPizza
                            :query [pizza NewSingleToppingPizza]
                            :summary "get echo of a pizza"
                            (ok pizza))
                      (PUT* "/anonymous" []
                            :return [{:secret Boolean s/Keyword s/Any}]
                            :body [body [{:secret Boolean s/Keyword s/Any}]]
                            (ok body))
                      (GET* "/hello" []
                            :return String
                            :query-params [name :- String]
                            (ok (str "Hello, " name)))
                      (POST* "/pizza" []
                             :return NewSingleToppingPizza
                             :body [pizza NewSingleToppingPizza]
                             :summary "post echo of a pizza"
                             (ok pizza)))

            (context* "/pizzas" []
                      :tags ["pizza"]

                      (GET* "/" []
                            :return [Pizza]
                            :summary "Gets all Pizzas"
                            (ok (get-pizzas)))
                      (POST* "/" []
                             :return Pizza
                             :body [pizza NewPizza {:description "new pizza"}]
                             :summary "Adds a pizza"
                             (ok (add! pizza)))
                      (PUT* "/" []
                            :return Pizza
                            :body [pizza Pizza]
                            :summary "Updates a pizza"
                            (ok (update! pizza)))
                      (GET* "/:id" []
                            :return Pizza
                            :path-params [id :- Long]
                            :summary "Gets a pizza"
                            (ok (get-pizza id)))
                      (DELETE* "/:id" []
                               :path-params [id :- Long]
                               :summary "Deletes a Pizza"
                               (ok (delete! id))))
            )