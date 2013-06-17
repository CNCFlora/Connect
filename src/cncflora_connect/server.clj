(ns cncflora-connect.server
  (:use ring.adapter.jetty
        ring.middleware.session.memory
        compojure.core
        stencil.core
        ring.util.response
        cncflora-connect.users
        cncflora-connect.roles
        cncflora-connect.search
        ring.middleware.cors
        [noir.cookies  :only [wrap-noir-cookies]]
        [clojure.data.json :only [read-str write-str]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [noir.session :as session]
            [clj-http.client :as http]))

(stencil.loader/set-cache
  (clojure.core.cache/ttl-cache-factory {}))

(connect "data")

(defn page 
  ""
  [html data]
  (println data)
  (render-file
    (str "templates/" html ".html")
    (assoc data
          :logged (session/get :logged) 
          :user   (session/get :user))))

(defn security
  ""
  [handler]
  (let [free ["/" "/api" "/_ca"
              "/login" "/register"
              "/img" "/css" "/js"]]
  (fn [req]
   (if (some true? (map #(.startsWith (:uri req) %) free))
     (handler req)
     (if (have-role? (session/get :user) "admin")
      (handler req)
        {:status 302 :headers
          {"Location" "/login"}})))))

(defroutes main
  (GET "/" [] (page "index" {}))

  (GET "/login" [] (page "login" {}))
  (POST "/logout" [] (session/clear!) (redirect "/"))

  (OPTIONS "/api/auth" []
     {:headers {"Allow" "POST,OPTIONS" 
                "Access-Control-Allow-Methods" "POST,OPTIONS" 
                "Access-Control-Allow-Headers" "x-requested-with"}
      :status 200})
  (OPTIONS "/api/logout" []
     {:headers {"Allow" "POST,OPTIONS" 
                "Access-Control-Allow-Methods" "POST,OPTIONS" 
                "Access-Control-Allow-Headers" "x-requested-with"}
      :status 200})
  (POST "/api/auth" {params :params}
    (println params)
    (let [persona (http/post "https://verifier.login.persona.org/verify"
                    {:form-params params :as :json})
          resp (:body persona)]
      (if (= "okay" (:status resp))
        (let [user (find-by-email (:email resp))]
          (if (valid-user? user)
            (do (session/put! :logged true) 
                (session/put! :user user)
                (write-str user))
            (write-str {:status "nok"})))
        (write-str {:status "nok"}))))
  (POST "/api/logout" []
    (session/clear!) (write-str {}))
  (GET "/api/user" []
   (let [user (session/get :user)
         roles (assign-tree user)]
    (write-str (assoc user :roles roles))))

  (GET "/register" [email] (page "register" {}))
  (POST "/register" {user :params} 
    (if-not (valid-new-user? user)
      (redirect "/register-bad")
      (do 
        (create-user user)
        (if-not (have-admin?)
          (do
            (approve-user user)
            (register-role "admin")
            (assign-role user "admin")))
        (redirect "/register-ok"))))
  (GET "/register-ok" [] (page "register-ok" {}))
  (GET "/register-bad" [] (page "register-bad" {}))

  (GET "/dashboard" [] 
   (page "dashboard" {:pendding (get-pendding)}))

  (GET "/user/:uuid" [uuid] 
   (page "user" {:profile_user (find-by-uuid uuid)
                 :roles (assign-tree (find-by-uuid uuid))}))
  (POST "/user/:uuid" {user :params }
    (update-user user)
    (page "user" {:profile_user (find-by-uuid (:uuid user))
                  :roles (assign-tree (find-by-uuid (:uuid user)))
                  :message {:type "success" :message "Salvo com sucesso" }}))
  (GET "/users/:pg" [pg]
    (let [pg (Integer. pg)]
      (page "users" {:users (get-users pg)
                     :prev  (if (> 0 pg) (dec pg))
                     :next  (if (< (inc pg ) (/ (count (get-users)) 20)) (inc pg))})))
  (GET "/pendding" [pg]
    (page "pendding" {:pendding (get-pendding)}))

  (POST "/users" {user :params}
    (create-user user)
    (approve-user (find-by-email (:email user)))
    (redirect "/users/0"))

  (POST "/user/:uuid/assign/role" {params :params}
    (assign-role (find-by-uuid (:uuid params)) (:role params))
    (redirect (str "/user/" (:uuid params))))
  (POST "/user/:uuid/assign/entity" {params :params}
    (assign-entity (find-by-uuid (:uuid params)) (:role params) (:entity params))
    (redirect (str "/user/" (:uuid params))))
  (GET "/user/:uuid/unassign/role/:role" {params :params}
    (unassign-role (find-by-uuid (:uuid params)) (:role params))
    (redirect (str "/user/" (:uuid params))))
  (GET "/user/:uuid/unassign/role/:role/entity/:entity" {params :params}
    (unassign-entity (find-by-uuid (:uuid params)) (:role params) (:entity params))
    (redirect (str "/user/" (:uuid params))))

  (GET "/search" {params :params}
    (page "search" {:q (:q params)
                    :users (search (:q params))}))
  (GET "/search/roles" {params :params}
    (write-str (map #(hash-map :label (:role %) :value (:role %)) (find-role (:term params)))))
  (GET "/search/entities" {params :params}
    (write-str (map #(hash-map :label (:name %) :value (:value %)) (find-entity (:term params)))))



  (GET "/roles" []
    (page "roles" {:roles (map #(hash-map :role %1) (list-roles))}))
  (GET "/roles/:role/delete" [role]
    (page "roles-delete" {:role role}))
  (POST "/roles/:role/delete" {params :params}
    (remove-role (:role params))
    (redirect "/roles"))
  (GET "/roles/:role/users" [role]
    (page "roles-users" {:role role :users (find-users-of-role role)}))
  (POST "/roles" {role :params}
    (register-role (:role role))
    (redirect "/roles"))


  (GET "/entities/:pg" [pg]
    (let [pg (Integer. pg)]
      (page "entities" {:entities (list-entities)
                        :prev (if (> 0 pg) (dec pg))
                        :next (if (< (inc pg) (/ (count (list-entities)) 20)) (inc pg))}
            )))
  (GET "/entities/:entity/delete" [entity]
    (page "entities-delete" {:entity entity}))
  (POST "/entities/:entity/delete" {params :params}
    (remove-entity (:entity params))
    (redirect "/entities/0"))
  (POST "/entities" {entity :params}
    (register-entity entity)
    (redirect "/entities/0"))

  (POST "/_ca" {user :params}
    (if (have-admin?)
      {:status 400 :body "{\"success\":false,\"error\":\"Admin already exists.\"}"}
      (do
        (create-user {:email (:email user) :name (:name user)})
        (approve-user user)
        (register-role "admin")
        (assign-role user "admin") 
        {:status 201 :body "{\"success\":true}"})))

  (route/resources "/"))

(def app
  (-> (handler/site main)
      (wrap-cors :access-control-allow-origin #".*")
      (security)
      (wrap-noir-cookies)
      (session/wrap-noir-session 
        {:store (memory-store session/mem)})))

(defn -main
  ""
  [& args]
  (run-jetty app {:port 8081 :join? false}))
