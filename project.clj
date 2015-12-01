(defproject flora-connect "0.3.2"
  :description "Single Sign On for the CNCFlora systems"
  :url "http://github.com/CNCFlora/connect"
  :main flora-connect.server
  :ring { :handler flora-connect.server/app
          :init flora-connect.server/start
          :destroy flora-connect.server/start
          :reload-paths ["src"] }
  :resources-path "resources"
  :license {:name "MIT" }
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.apache.derby/derby "10.10.2.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [stencil "0.5.0"]
                 [lib-noir "0.9.9"]
                 [org.clojure/data.json "0.2.6"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [com.draines/postal "1.11.3"]
                 [clj-http "2.0.0"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.8.2"]
                                  [javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.7"]
                             [lein-midje "3.1.3"]]}})
