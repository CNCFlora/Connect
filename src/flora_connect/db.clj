(ns flora-connect.db
  (:require [clojure.java.jdbc :as j]))


(def db-file (java.io.File. "/var/floraconnect/users"))

(def db
    {
     :classname   "org.apache.derby.jdbc.EmbeddedDriver"
     :subprotocol "derby"
     :subname     (str (.getAbsolutePath db-file) )
    }
  )

(if-not (.exists db-file)
  (j/db-do-commands (assoc db :create true)
    (j/create-table-ddl :users
      [:uuid "VARCHAR(255)"]
      [:email "VARCHAR(255)"]
      [:status "VARCHAR(255)"]
      [:password "VARCHAR(255)"])
    (j/create-table-ddl :users_data
      [:uuid "VARCHAR(255)"]
      [:name "VARCHAR(255)"]
      [:institute "VARCHAR(255)"]
      [:phone "VARCHAR(255)"]
      [:address "VARCHAR(1500)"]
      [:postal "VARCHAR(255)"]
      [:state "VARCHAR(255)"]
      [:city "VARCHAR(255)"]
      [:complement "VARCHAR(255)"])
    (j/create-table-ddl :user_role_entity
      [:uuid "VARCHAR(255)"]
      [:role "VARCHAR(255)"]
      [:entity "VARCHAR(255)"])
    ))

(defn execute!
  ([db q] 
   (println q) 
   (j/execute! db q))
  ([db q params] 
   (let [params (vec (flatten [q params]))]
     (println params)
     (println (j/execute! db params)))) )

(defn query!
  ([db q] 
   (println q) 
   (j/query db q))
  ([db q params] 
   (let [params (vec (flatten [q params]))]
     (println params)
     (j/query db params))))

(defn get!
  ([db table k v] 
    (query! db 
      (str "SELECT * FROM " (name table) " WHERE " (name k) "=?") [v])))

(defn create!
 [db table data]
   (let [fields (map name (map key data))
         values (map val data)]
   (execute! db
     (str "INSERT INTO " (name table) 
          " (" (apply str (interpose "," fields )) ") "
          " VALUES (" (apply str (interpose "," (for [i (range 0 (count values))] "?"))) ")" )
       values)))

(defn update!
  [db table data]
   nil)

(defn delete!
 [db table data]
   (let [fields (map name (map key data))
         values (map val data)]
   (execute! db
     (str "DELETE FROM " (name table) " WHERE "
           (apply str (interpose "=? AND " fields)) "=?")
       values)))

