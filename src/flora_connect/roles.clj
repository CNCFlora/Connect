(ns flora-connect.roles
  (:use simple-cypher.core)
  (:use flora-connect.users))

(defn register-role
  ""
  [role] 
  (if (empty?
       (query! db 
        (str "START role=node:nodes(role='" role "')
              RETURN role")))
    (create! db {:role role :name role :type "role"} :index)))

(defn remove-role
  ""
  [role]
  (if-not (= "admin" role)
    (do
      (query! db 
        (str "START r=node:nodes(role='" role "')"
             "     ,u=node:nodes(type='user')"
             " MATCH u-[rel:ASSIGNED]->e"
             " WHERE rel.role = '" role "'"
             " DELETE rel"))
      (delete! db (first (get! db :role role :raw))))))

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

(defn have-role?
  ""
  [user role] 
  (not (empty?
   (query! db
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
         " ,r=node:nodes(role='" role "')"
         " MATCH u-[rel:IS]->r"
         " RETURN rel")))))

(defn have-access?
  ""
  [user role ent] 
    (not (empty?
     (query! db
      (str "START u=node:nodes(uuid='" (:uuid user) "')"
           " ,e=node:nodes(value='" ent "')"
           " MATCH u-[rel:ASSIGNED]->e"
           " WHERE rel.role = '" role "'"
           " RETURN rel")))))

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
 (if-not (have-role? user role) 
  (query! db 
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
              " , r=node:nodes(role='" role "')"
         " CREATE u-[:IS]->r"
         " RETURN u , r"))))

(defn assign-entity
 ""
 [user role entity]
 (if-not (have-access? user role entity)
  (query! db 
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
              " ,e=node:nodes(value='" entity "')"
         " CREATE u-[:ASSIGNED{role:'" role "'}]->e"))))

(defn unassign-entity
  ""
  [user role entity]
  (query! db 
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
              " ,e=node:nodes(value='" entity "')"
         " MATCH u-[rel:ASSIGNED]->e"
         " WHERE rel.role = '" role "'"
         " DELETE rel")))

(defn unassign-role
 ""
 [user role]
  (query! db 
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
              " ,r=node:nodes(role='" role "')"
         " MATCH u-[rel:IS]->r"
         " DELETE rel"))
  (query! db 
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
         " MATCH u-[rel:ASSIGNED]->e"
         " WHERE rel.role = '" role "'"
         " DELETE rel")))

(defn assign-tree
  ""
  [user] 
  (let [roles  (query! db
                (str "START u=node:nodes(uuid='" (:uuid user) "')"
                     " MATCH u-[:IS]->r"
                     " return r.role as role"))
        entities (query! db
                  (str "START u=node:nodes(uuid='" (:uuid user) "')"
                       " MATCH u-[rel:ASSIGNED]->e"
                       " RETURN rel.role as role,
                          e.name as name,
                          e.value as value"))]
    (map (fn [r] {:role r 
                  :entities (map #(dissoc % :role)
                             (filter #(= (:role %) r) entities)) })
         (map :role roles))))


(defn user-assignments
  ""
  [user]
  (query! db
    (str "START u=node:nodes(uuid='" (:uuid user) "')"
         " MATCH assigns=u-[rel:ASSIGNED]->e"
         " RETURN rel.role as role, e.value as entity"
         )))

(defn find-role
  ""
  [part] 
  (map :r (query! db
    (str "START r=node:nodes(\"role:*" part "*\")"
         " RETURN r") )))

(defn find-entity
  ""
  [part] 
  (map :e (query! db
    (str "START e=node:nodes(\"name:*" part "*\")"
         " RETURN e") )))

(defn find-users-of-role
  ""
  [role]
   (map :u (query! db
    (str "START u=node:nodes(type='user')"
         " , r=node:nodes(role='" role "')"
         " MATCH u-[:IS]->r"
         " RETURN u"))))

(defn -clear
  ""
  [] (for [n (get! db :type "entity" :raw)]
       (delete! db n)))

