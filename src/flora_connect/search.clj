(ns flora-connect.search
  (:use simple-cypher.core)
  (:use flora-connect.users)
  (:use flora-connect.roles))

(defn search [n]
 (distinct
  (filter #(= "user" (:type %))
   (flatten
    (map #(vector (:n %) (:m %))
     (apply concat
      (for [n (.split n " ")]
       (concat
        (query! db
         (str "START n=node:nodes(\"email:*" n "*\")"
              " MATCH n<-[r?]-m"
              " RETURN n,m"))
        (query! db
         (str "START n=node:nodes(\"name:*" n "*\")"
              " MATCH n<-[r?]-m"
              " RETURN n,m"))))))))))

