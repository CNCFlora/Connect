(defproject cncflora-connect "0.0.1"
  :description ""
  :url ""
  :main cncflora-connect.server
  :ring {:handler cncflora-connect.server/app}
  :resources-path "resources"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [simple-cypher "0.0.1"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [stencil "0.3.1"]
                 [lib-noir "0.4.6"]
                 [org.clojure/data.json "0.2.0"]
                 [clj-http "0.7.1"]]
  :profiles {:dev {:dependencies [[midje "1.5-RC1"]]}})
