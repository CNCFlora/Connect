(ns flora-connect.ui
  (:use flora-connect.db
        flora-connect.users
        flora-connect.roles
        flora-connect.search
        flora-connect.web-wrap
        stencil.core
        compojure.core
        ring.util.response
        [clojure.data.json :only [read-str write-str]])
  (:require [noir.session :as session]))

(stencil.loader/set-cache
  (clojure.core.cache/ttl-cache-factory {}))

(defn page 
  ""
  [html data]
  #_(println data)
  (render-file
    (str "templates/" html ".html")
    (assoc data
          :base   @context-path
          :logged (session/get :logged) 
          :admin  (session/get :admin)
          :user   (session/get :user))))

(defroutes app

  (GET "/" [] 
    (if (have-admin?)
     (redirect "/index")
     (redirect "/register")))

  (GET "/index" []
     (if (have-admin?)
       (page "index" {})
       (redirect "/register")))

  (GET "/connect" [] (page "connect" {}))

  (POST "/logout" [] (session/clear!) (redirect "/"))

  (GET "/register" [email] (page "register" {}))

  (POST "/register" {user :params} 
    (if-not (valid-new-user? user)
      (redirect "/register-bad")
      (do 
        (create-user user)
        (if-not (have-admin?)
          (do
            (approve-user user)
            (add-context  "connect")
            (add-role "admin")
            (assign-role (find-by-email (:email user)) "connect" "admin")))
        (redirect "/register-ok"))))

  (GET "/register-ok" [] (page "register-ok" {}))
  (GET "/register-bad" [] (page "register-bad" {}))

  (GET "/login-test" [] (page "login-test" {:got false}))

  (POST "/login-test" {params :params} 
    (page "login-test" {:got true :found (search (:email params))}))

  (GET "/recover" [] (page "recover" {}))
  (POST "/recover" {user :params} 
        (let [new-pass (apply str "cnc-" (for [n (range 4)] (rand-int 9)))
              user     (find-by-email (:email user))]
          (comment (update-pass (assoc user :password new-pass)))
          (redirect "/recover-ok")))
  (GET "/recover-ok" [] (page "recover-ok" {}))

  (GET "/dashboard" [] 
   (page "dashboard" {:user (session/get :user)}))

  (GET "/edit" []
    (page "user-edit" {}))
  (POST "/edit" {user :params}
    (update-user user)
    (session/put! :user user)
    (redirect "/dashboard"))
  (POST "/edit/pass" {form :params}
        (update-pass form)
        (redirect (str "/dashboard")))


  (GET "/user/:uuid" [uuid] 
   (page "user" {:profile_user (find-by-uuid uuid)
                 :contexts (assign-tree (find-by-uuid uuid))}))
  (POST "/user/:uuid" {user :params}
    (update-user user)
    (page "user" {:profile_user (find-by-uuid (:uuid user))
                  :roles (assign-tree (find-by-uuid (:uuid user)))
                  :message {:type "success" :message "Salvo com sucesso" }}))
  (POST "/user/:uuid/pass" {form :params}
        (update-pass form)
        (redirect (str "/user/" (:uuid form))))

  (GET "/users/:pg" [pg]
    (let [pg (Integer. pg)]
      (page "users" {:users (get-users pg)
                     :prev  (if (> pg 0) (dec pg) false)
                     :next  (if (< (inc pg) (/ (count (get-users)) 20)) (inc pg) false)})))

  (GET "/pendding" [pg]
    (page "pendding" {:pendding (get-pendding)}))

  (POST "/users" {user :params}
    (create-user user)
    (approve-user (find-by-email (:email user)))
    (redirect "/users/0"))

  (POST "/user/:uuid/assign/role" {params :params}
    (assign-role (find-by-uuid (:uuid params)) (:context params) (:role params))
    (redirect (str "/user/" (:uuid params))))
  (POST "/user/:uuid/assign/entity" {params :params}
    (assign-entity (find-by-uuid (:uuid params)) (:context params) (:role params) (:entity params))
    (redirect (str "/user/" (:uuid params))))
  (GET "/user/:uuid/unassign/context/:context" {params :params}
    (unassign-context (find-by-uuid (:uuid params)) (:context params))
    (redirect (str "/user/" (:uuid params))))
  (GET "/user/:uuid/unassign/context/:context/role/:role" {params :params}
    (unassign-role (find-by-uuid (:uuid params)) (:context params) (:role params))
    (redirect (str "/user/" (:uuid params))))
  (GET "/user/:uuid/unassign/context/:context/role/:role/entity/:entity" {params :params}
    (unassign-entity (find-by-uuid (:uuid params)) (:context params) (:role params) (:entity params))
    (redirect (str "/user/" (:uuid params))))

  (GET "/roles" []
    (page "roles" {:roles (list-roles)}))
  (POST "/roles" {params :params}
    (add-role (:role params))
    (redirect "/roles"))
  (GET "/roles/:role/del" [role]
    (del-role role)
    (redirect "/roles"))
  (GET "/roles/:role" [role]
     (page "/role-in" {:users (find-users-of-role role)}))

  (GET "/contexts" []
    (page "contexts" {:contexts (list-contexts)}))
  (POST "/contexts" {params :params}
    (add-context (:context params))
    (redirect "/contexts"))
  (GET "/contexts/:context/del" [context]
    (del-context context)
    (redirect "/contexts"))
  (GET "/contexts/:context" [context]
     (page "/context-in" {:users (find-users-of-context context)}))

  (GET "/search" {params :params}
    (page "search" {:q (:q params)
                    :users (search (:q params))}))
  (GET "/search/contexts" {params :params}
    (write-str (map #(hash-map :label % :value %) (find-context (:term params)))))
  (GET "/search/roles" {params :params}
    (write-str (map #(hash-map :label % :value %) (find-role (:term params)))))
  (GET "/search/entities" {params :params}
    (write-str (map #(hash-map :label % :value %) (find-entity (:term params)))))

  )

