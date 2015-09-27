(ns chimera.reasoning.user
  (:require [schema.core :as s]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [cemerick.friend :as friend]
            [friend-oauth2.util :refer [format-config-uri]]
            [ring.swagger.schema :refer [coerce!]]
            )
  )


(s/defschema User {:id        Long
                   :timestamp Long
                   :type      String
                   :device_id Long
                   :value     Long})

(s/defschema NewUser (dissoc User :id))


(defonce user-id-seq (atom 0))
(defonce users (atom (array-map)))

(defn get-user [id] (@users id))
(defn list-users [] (-> users deref vals reverse))
(defn delete-user! [id] (swap! users dissoc id) nil)

(defn create-user! [new-user]
  (let [id (swap! user-id-seq inc)
        user (coerce! User (assoc new-user :id id))]
    (swap! users assoc id user)
    user))

(defn update-user! [user]
  (let [user (coerce! User user)]
    (swap! users assoc (:id user) user)
    (get-user (:id user))))

;; Data

(when (empty? @users)
  (create-user! {:type "temperature" :value 950 :timestamp 1 :device_id 2})
  (create-user! {:type "temperature" :value 12 :timestamp 1 :device_id 2}))

;(defroutes* user-routes
;            (context* "/users" []
;                      :tags ["user"]
;                      (GET* "/" [] :return [User] (ok (list-users)))
;                      (POST* "/" [] :return User :body [user NewUser] (ok (create-user! user)))
;                      (PUT* "/" [] :return User :body [user User] (ok (update-user! user)))
;                      (GET* "/:id" [] :return User :path-params [id :- Long] (ok (get-user id)))
;                      (DELETE* "/:id" [] :path-params [id :- Long] (ok (delete-user! id)))))
;(ns chimera.reasoning.user
;  (:require [schema.core :as s]
;            [compojure.api.sweet :refer :all]
;            [ring.util.http-response :refer :all]
;            [ring.swagger.schema :refer [coerce!]]
;            [compojure.api.sweet :refer [defroutes] ]
;             [compojure.core :refer :all]
;            [friend-oauth2.workflow :as oauth2]
;            [friend-oauth2.util :refer [format-config-uri]]
;            [cheshire.core :as j]
;            (cemerick.friend [workflows :as workflows]
;                             [credentials :as creds])))
;
(defn credential-fn
  [token]
  ;;lookup token in DB or whatever to fetch appropriate :roles
  {:identity token :roles #{::user}})

(def client-config
  {:client-id     "96889343147-esv3i64rjkaiogkvsafm1p1qao3dj0r8.apps.googleusercontent.com"
   :client-secret "GbAQnL0KRBlsx6z2e371LbCY"
   :callback      {:domain "http://localhost:3000" :path "/oauth2callback"}})

(def uri-config
  {:authentication-uri {:url   "https://accounts.google.com/o/oauth2/auth"
                        :query {:client_id     (:client-id client-config)
                                :response_type "code"
                                :redirect_uri  (format-config-uri client-config)
                                :scope         "email"}}

   :access-token-uri   {:url   "https://accounts.google.com/o/oauth2/token"
                        :query {:client_id     (:client-id client-config)
                                :client_secret (:client-secret client-config)
                                :grant_type    "authorization_code"
                                :redirect_uri  (format-config-uri client-config)}}})

(defroutes* user-routes
            (context*
              "/user" []
              :tags ["user"]
              (GET* "/" request "open.")
              (GET* "/status" request
             (let [count (:count (:session request) 0)
                   session (assoc (:session request) :count (inc count))]
               (-> (ring.util.response/response
                     (str "<p>We've hit the session page " (:count session)
                          " times.</p><p>The current session: " session "</p>"))
                   (assoc :session session))))
              (GET* "/authlink" request
             (friend/authorize #{::user} "Authorized page."))
              (GET* "/authlink2" request
             (friend/authorize #{::user} "Authorized page 2."))
              (GET* "/admin" request
             (friend/authorize #{::admin} "Only admins can see this page."))
              (friend/logout (ANY* "/logout" request (ring.util.response/redirect "/"))))
            )

;;(def app
;;  (handler/site
;;    (friend/authenticate
;;      user-routes
;;      {:allow-anon? true
;;       :workflows   [(oauth2/workflow
;;                       {:client-config client-config
;;                        :uri-config    uri-config
;;                        :credential-fn credential-fn})]})))
