(defproject flora-connect "0.2.3"
  :description "Single Sign On for the CNCFlora systems"
  :url "http://github.com/CNCFlora/connect"
  :main flora-connect.server
  :ring { :handler flora-connect.server/app
          :init flora-connect.server/start
          :destroy flora-connect.server/start
          :reload-paths ["src"] }
  :resources-path "resources"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [org.apache.derby/derby "10.10.2.0"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [stencil "0.3.2"]
                 [lib-noir "0.4.6"]
                 [org.clojure/data.json "0.2.0"]
                 [ring-cors "0.1.1"]
                 [com.draines/postal "1.11.1"]
                 [clj-http "0.7.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-ring "0.8.6"]
                             [lein-midje "3.0.0"]]}})
