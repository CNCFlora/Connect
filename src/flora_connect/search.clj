(ns flora-connect.search
  (:use simple-cypher.core)
  (:use flora-connect.users)
  (:use flora-connect.roles))

(defn search [n]
  (filter #(= "user" (:type %))
   (flatten
    (map #(vector (:n %) (:m %))
     (query! db
       (str "START n=node:nodes(\"name:*" n "*\")"
            " MATCH n<-[r?]-m"
            " RETURN n,m"))))))

