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
  [role]
  (query! db 
    (str "START r=node:nodes(role='" role "')"
         "     ,u=node:nodes(type='user')"
         " MATCH u-[rel:ASSIGNED]->e"
         " WHERE rel.role = '" role "'"
         " DELETE rel"))
  (delete! db (first (get! db :role role :raw))))

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
                RETURN ent"))))

(defn assign-role
 ""
 [user role]
  (query! db 
    (str "START u=node:nodes(email='" (:email user) "')"
              " ,r=node:nodes(role='" role "')"
         " CREATE u-[:IS]->r")))

(defn assign-entity
 ""
 [user role entity]
  (query! db 
    (str "START u=node:nodes(email='" (:email user) "')"
              " ,e=node:nodes(value='" entity "')"
         " CREATE u-[:ASSIGNED{role:'" role "'}]->e")))

(defn unassign-entity
  ""
  [user role entity]
  (query! db 
    (str "START u=node:nodes(email='" (:email user) "')"
              " ,e=node:nodes(value='" entity "')"
         " MATCH u-[rel:ASSIGNED]->e"
         " WHERE rel.role = '" role "'"
         " DELETE rel")))

(defn unassign-role
 ""
 [user role]
  (query! db 
    (str "START u=node:nodes(email='" (:email user) "')"
              " ,r=node:nodes(role='" role "')"
         " MATCH u-[rel:IS]->r"
         " DELETE rel"))
  (query! db 
    (str "START u=node:nodes(email='" (:email user) "')"
         " MATCH u-[rel:ASSIGNED]->e"
         " WHERE rel.role = '" role "'"
         " DELETE rel")))

(defn user-assignments
  ""
  [user]
  (query! db
    (str "START u=node:nodes(email='" (:email user) "')"
         " MATCH assigns=u-[rel:ASSIGNED]->e"
         " RETURN rel.role as role, e.value as entity"
         )))

(defn find-entity
  ""
  [part] 
  (map :e (query! db
    (str "START e=node:nodes(\"name:*" part "*\")"
         " RETURN e") )))

(defn -clear
  ""
  [] (for [n (get! db :type "entity" :raw)]
       (delete! db n)))

