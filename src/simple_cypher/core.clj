(ns simple-cypher.core
  (:import [org.neo4j.kernel EmbeddedGraphDatabase]
           [org.neo4j.graphdb RelationshipType] 
           [org.neo4j.graphdb.factory GraphDatabaseFactory]
           [org.neo4j.cypher.javacompat ExecutionEngine]
           [org.neo4j.helpers.collection MapUtil]
           [org.neo4j.graphdb.index IndexManager]))

(def graphs (atom {})) 

(defn -register-shutdown 
  "A shutdown function will be assigned to properly close
   the db when needed"
  [db] (.addShutdownHook (Runtime/getRuntime)
        (Thread. #(.shutdown db)))) 

(defn graph 
  "First you need to get your self a graph instance.
   Provide us the path and, if not already loaded, 
   we will provide embeded graph for you"
  [path]
  (if-let [graph (@graphs path)] graph
    (let [graph (.newEmbeddedDatabase (GraphDatabaseFactory.) path)]
      (-register-shutdown graph) 
      (swap! graphs (fn [graphs] (assoc graphs path graph))) 
      graph))) 

(defn create!
  "You can now create nodes, simply give the graph and data for the node
   And optionaly :index if wanted it fully indexed"
  [graph data & idx]
  (let [tx (.beginTx graph)]
    (let [node  (.createNode graph) 
          index (.forNodes (.index graph) "nodes" 
                  (hash-map  "type" "fulltext" "provider" "lucene"))]
      (dorun (for [kv data] (.setProperty node (name (key kv)) (val kv))))
      (if (= (first idx) :index)
        (dorun (for [kv data] 
                 (.add index node (name (key kv)) (val kv))))) 
      (.success tx)
      (.finish tx)
      node)))

(defn -node-data [node] 
  (reduce #(merge %1 %2) {}
    (for [k (.getPropertyKeys node)]
      (hash-map (keyword k) (.getProperty node k)))))

(defmulti node-data class) 
(defmethod node-data org.neo4j.graphdb.Node [node]
  (-node-data node)) 
(defmethod node-data org.neo4j.kernel.impl.core.NodeProxy [node]
  (-node-data node)) 
(defmethod node-data :default [n] n) 

(defn get!
  "Retrieve a node from the index. You should be using cypher by now."
  [graph k v & raw]
  (let [index (.forNodes  (.index graph) "nodes")
        nodes (.get index (name k) v)]
    (for [node nodes]
      (if (= (first raw) :raw) node
        (node-data node)))))

(defn relate! 
  "Relate two nodes with a type"
  [g n1 rel n2]
  (let [tx (.beginTx g)]
    (.createRelationshipTo n1 n2
       (proxy [RelationshipType] [] 
         (name [] (name rel))))
    (.success tx)
    (.finish tx))) 

(defn delete!
  "You can also delete a node"
  [g node]
  (let [tx (.beginTx g)]
    (dorun (for [r (.getRelationships node)] (.delete r)))
    (.delete node) 
    (.success tx)
    (.finish tx))) 

(defn query! 
  "Perform given cypher query on graph and return the resulting nodes and relations"
  [g query]
  (let [eng (ExecutionEngine. g)
        res (.execute eng query)]
   (reduce conj [] 
    (for [row res]
     (do 
      (reduce merge
        (map (fn [col] {(keyword (.getKey col)) (node-data (.getValue col))}) row)))))))

