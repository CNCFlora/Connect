(ns flora-connect.server
  (:use ring.adapter.jetty
        ring.middleware.session.memory
        compojure.core
        stencil.core
        ring.util.response
        flora-connect.users
        flora-connect.roles
        flora-connect.search
        ring.middleware.cors
        [noir.cookies  :only [wrap-noir-cookies]]
        [clojure.data.json :only [read-str write-str]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [noir.session :as session]
            [clj-http.client :as http]))

(stencil.loader/set-cache
  (clojure.core.cache/ttl-cache-factory {}))

(defn start
  []
  (connect "/var/lib/floraconnect"))

(defn stop
  []
  (disconnect))

(def context-path (atom nil))

(defn page 
  ""
  [html data]
  (render-file
    (str "templates/" html ".html")
    (assoc data
          :base   @context-path
          :logged (session/get :logged) 
          :user   (session/get :user))))

(defn- get-context-path
    "Returns the context path when running as a servlet"
    ([] @context-path)
    ([servlet-req]
          (if (nil? @context-path)
             (reset! context-path
               (.getContextPath servlet-req)))
          @context-path))

(defn wrap-context
  ""
  [handler]
  (fn [req] 
    (if-let [servlet-req (:servlet-request req)]
      (let [context (get-context-path servlet-req)
            uri (:uri req)]
        (if (.startsWith uri context)
          (handler (assoc req :uri (.substring uri (.length context))))
          (handler req)))
      (handler req))))

(defn wrap-context-redir
  ""
  [handler] 
  (fn [req]
    (let [res (handler req)]
      (if (= 302 (:status res))
        (assoc-in res [:headers "Location"] (str @context-path (get-in res [:headers "Location"])))
        res))))

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

(defn jsonp
  ""
  [handler] 
   (fn [req] 
     (let [q (:query-string req)
           response (handler req)]
       (if (nil? q) response
         (if-not (.contains q "callback=") response
           (let [callback (second
                           (re-find #"callback=([a-zA-Z0-9-_]+)" 
                            (:query-string req)))]
               (assoc response :body (str callback "(" (:body response) ");")))
           )))))

(defn options
  ""
  [handler]
   (fn [req]
     (if (= "OPTIONS" (:method req))
       {:headers {"Allow" "GET,POST,OPTIONS" 
                  "Access-Control-Allow-Methods" "GET,POST,OPTIONS" 
                  "Access-Control-Allow-Headers" "x-requested-with"}
        :status 200}
       (handler req))))

(defroutes main
  (GET "/" [] 
    (if (have-admin?)
     (redirect "/index")
     (redirect "/register")))

  (GET "/index.html" []
     (if (have-admin?)
       (page "index" {})
       (redirect "/register") ))
  (GET "/index" []
     (redirect "/index.html"))

  (GET "/connect" [] (page "connect" {}))
  (GET "/login" [] (page "login" {}))
  (POST "/login" [])
  (POST "/logout" [] (session/clear!) (redirect "/"))

  (ANY "/api/auth" {user :params}
    (if (valid-user? user)
      (let [user (find-by-email (:email user))
            roles (assign-tree user)]
          (session/put! :logged true) 
          (session/put! :user user)
          (write-str (assoc user :roles roles)))
      (write-str {:status "nok"})
      ))
  (ANY "/api/logout" []
    (session/clear!) (write-str {}))
  (GET "/api/user" []
   (let [user (session/get :user)
         roles (assign-tree user)]
     (session/put! :foo "bar")
     (write-str (assoc user :roles roles))
     ))

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
      (wrap-context)
      (wrap-context-redir)
      (wrap-cors :access-control-allow-origin #".*")
      (security)
      (jsonp)
      (options)
      (wrap-noir-cookies)
      (session/wrap-noir-session 
        {:store (memory-store session/mem)})))

(defn -main
  ""
  [& args]
  (start)
  (run-jetty app {:port 8081 :join? true})
  (stop))
