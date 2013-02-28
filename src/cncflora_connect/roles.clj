(ns cncflora-connect.roles
  (:use simple-cypher.core)
  (:use cncflora-connect.users))

(defn register-role
  ""
  [role] 
  (if (empty?
       (query! db 
        (str "START role=node:nodes(role='" role "')
              RETURN role")))
    (create! db {:role role :type "role"} :index)))

(defn remove-role
  ""
  [role] (delete! db (first (get! db :role role :raw))))

(defn list-roles
  ""
  [] (map :role (map :role 
       (query! db "START role=node:nodes(type='role')
                   RETURN role"))))

(defn register-entity
  ""
  [entity]
  (if (empty?
       (query! db 
        (str "START ent=node:nodes(value='" (:value entity) "')
              RETURN ent")))
    (create! db (assoc entity :type "entity") :index)))

(defn remove-entity
  ""
  [entity] (delete! db (first (get! db :value entity :raw))))

(defn list-entities
  ""
  [] 
  (distinct 
   (map :ent
    (query! db "START ent=node:nodes(type='entity')
                RETURN ent"))) )

(defn clear
  ""
  [] (for [n (get! db :type "entity" :raw)]
       (delete! db n)))
