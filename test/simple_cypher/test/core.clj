(ns simple-cypher.test.core
  (:use simple-cypher.core
        midje.sweet))

(fact "Can create/get on graph"
 (let [g (graph "matrix")
       n (create! g {:name "Neo" :oldName "Anderson"} :index)]
   ((last (get! g :name "Neo")) :oldName) => "Anderson"
   (delete! g n)))

(fact "Can relate and query"
  (let [g (graph "matrix")
        t (create! g {:name "Trinity"} :index)
        n (create! g {:name "Neo"} :index)]
    (relate! g n :loves t)
    (let [r (query! g "START neo=node:nodes(name='Neo')
                       MATCH neo-[:loves]-trinity 
                       RETURN neo, trinity.name")] 
      ((first r) :trinity.name) => "Trinity"
      ((first r) :neo) => {:name "Neo"})
    (delete! g n)
    (delete! g t)))

