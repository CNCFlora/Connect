(ns flora-connect.api
  (:use flora-connect.db
        flora-connect.users
        flora-connect.roles
        flora-connect.search
        stencil.core
        compojure.core
        ring.util.response
        [clojure.data.json :only [read-str write-str]])
  (:require [noir.session :as session]))

(defroutes app

  (ANY "/auth" {params :params}
    (println "API auth:" params)
    (if (valid-user? params)
      (let [user0 (find-by-email (:email params))
            roles (assign-tree user0)
            user  (assoc user0 :roles roles)]
          (session/put! :logged true) 
          (session/put! :user user)
          (session/put! :admin (have-role? user "connect" "admin"))
          (println "API auth ok:" user)
          (write-str (assoc user :token (create-token user (:context params)))))
      (do
        (println "API Auth not ok.")
        (write-str {:status "nok"}))))

  (ANY "/logout" []
    (session/clear!) (write-str {}))

  (GET "/user" {params :params}
   (let [user (session/get :user)
         roles (assign-tree user)
         token (create-token user (:context params))]
     (write-str (assoc user :roles roles :token token))))

  (ANY "/token" {params :params}
       (println "API Req Token:" params)
    (if-let [user0 (find-by-token (:token params))]
      (let [roles (assign-tree user0 (:context user0))
            user  (assoc user0 :roles roles)]
          (println "API Token user:" user)
          (write-str user))
      (do
        (println "API Token no user.")
        (write-str {:status "nok"}))))

  )

