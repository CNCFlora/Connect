(ns flora-connect.server
  (:use ring.adapter.jetty
        ring.util.response
        ring.middleware.session.memory
        compojure.core
        flora-connect.db
        flora-connect.web-wrap
        [noir.cookies  :only [wrap-noir-cookies]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [flora-connect.ui :as ui]
            [flora-connect.api :as api]
            [noir.session :as session]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn start
  "" 
  [] (connect "/var/floraconnect/users"))

(defn stop
  ""
  [] nil)

(defroutes main

  ui/app

  (context "/api" [] api/app)

  (route/resources "/"))

(def app
  (-> (handler/site main)
      (wrap-context)
      (wrap-context-redir)
      (wrap-proxy-redir)
      (wrap-jsonp)
      (wrap-options)
      (wrap-noir-cookies)
      (wrap-cors :access-control-allow-origin #".*")
      (session/wrap-noir-session 
        {:store (memory-store session/mem)
         :cookie-name "ring-connect-session"})))

(defn -main
  ""
  [& args]
  (start)
  (run-jetty app {:port 3000 :join? true})
  (stop))

