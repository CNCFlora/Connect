(ns cncflora-connect.server
  (:use ring.adapter.jetty
        ring.middleware.session.memory
        compojure.core
        stencil.core
        ring.util.response
        cncflora-connect.users
        cncflora-connect.roles
        [noir.cookies  :only [wrap-noir-cookies]]
        [clojure.data.json :only [read-str write-str]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [noir.session :as session]))

(stencil.loader/set-cache
  (clojure.core.cache/ttl-cache-factory {}))

(defn page 
  ""
  [html data]
  (render-file
    (str "templates/" html ".html")
    (assoc data
          :logged (session/get :logged) 
          :user   (if (session/get :user)
                    (find-by-email (session/get :user)) ))))

(defn security
  ""
  [handler]
  (fn [req]
   (if (not (nil? (some #{(:uri req)} ["/" "/login" "/register" "/login-bad" "/register-bad" "/_ca"]))) 
     (handler req)
     (if (or (.startsWith (:uri req) "/img")
             (.startsWith (:uri req) "/css")
             (.startsWith (:uri req) "/js"))
       (handler req)
       (if (session/get :logged) 
       (handler req)
       {:status 301 :headers
        {"Location" "/login"}})))))

(defroutes main
  (GET "/" [] (page "index" {}))

  (GET "/login" [] (page "login" {}))
  (POST "/login" {user :params}
    (if (valid-user? user)
      (do (session/put! :logged true) 
          (session/put! :user (:email user))
          (redirect "/dashboard"))
      (redirect "/login-bad")))
  (GET "/login-bad" [] (page "login-bad" {}))
  (POST "/logout"[] (session/clear!) (redirect "/"))


  (GET "/register" [] (page "register" {}))
  (POST "/register" {user :params} 
    (if-not (valid-new-user? user)
      (redirect "/register-bad")
      (do
        (create-user user)
        (session/put! :logged true)
        (session/put! :user (:email user))
        (redirect "/register-ok"))))
  (GET "/register-ok" [] (page "register-ok" {}))
  (GET "/register-bad" [] (page "register-bad" {}))


  (GET "/users" [] (page "users" {:users (get-users)}))
  (GET "/dashboard" [] (page "dashboard" {}))


  (POST "/_ca" {user :params}
    (if (have-admin?)
      {:status 400 :body "{\"success\":false,\"error\":\"Admin already exists.\"}"}
      (do
        (create-user user)
        (approve-user user)
        (register-role "admin")
        (assign-role user "admin") 
        {:status 201 :body "{\"success\":true}"})))

  (route/resources "/"))

(def app
  (-> (handler/site main)
      (security)
      (wrap-noir-cookies)
      (session/wrap-noir-session 
        {:store (memory-store session/mem)})))

(defn -main
  ""
  [& args]
  (run-jetty app {:port 8081 :join? false}))
