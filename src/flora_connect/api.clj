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

  (ANY "/auth" {user :params}
    (if (valid-user? user)
      (let [user0 (find-by-email (:email user))
            roles (assign-tree user0)
            user  (assoc user0 :roles roles)]
          (session/put! :logged true) 
          (session/put! :user user)
          (session/put! :admin (have-role? user "connect" "admin"))
          (write-str (assoc user :token (create-token user (:context user)))))
      (write-str {:status "nok"})))

  (ANY "/logout" []
    (session/clear!) (write-str {}))

  (GET "/user" []
   (let [user (session/get :user)
         roles (assign-tree user)]
     (session/put! :foo "bar")
     (write-str (assoc user :roles roles))))

  (GET "/token" {params :params}
    (if-let [user0 (find-by-token (:email user))]
      (let [roles (assign-tree user0 (:context user0))
            user  (assoc user0 :roles roles)]
          (write-str (assoc user :token (create-token user))))
      (write-str {:status "nok"})))

  )
