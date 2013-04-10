(ns cncflora-connect.search
  (:use simple-cypher.core)
  (:use cncflora-connect.users)
  (:use cncflora-connect.roles))

(defn search [n]
  (filter #(= "user" (:type %))
   (flatten
    (map #(vector (:n %) (:m %))
     (query! db
       (str "START n=node:nodes(\"name:*" n "*\")"
            " MATCH n<-[r?]-m"
            " RETURN n,m"))))))

