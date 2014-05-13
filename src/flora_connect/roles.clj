(ns flora-connect.roles
  (:use flora-connect.db)
  (:use flora-connect.users))

(defn list-roles
  ""
  ([] (query! db
        "select distinct(role) from user_role_entity;"))
  ([user] (query! db
        "select distinct(role) from user_role_entity where uuid = ?" [(:uuid user)])))

(defn have-role?
  ""
  [user role] 
  (not
    (empty?
     (query! db
       "select * from user_role_entity where uuid = ? and role = ?"
         [(:uuid user) role]))))

(defn have-access?
  ""
  [user role ent] 
  (not
    (empty?
     (query! db
       "select * from user_role_entity where uuid = ? and role = ? and entity = ?"
         [(:uuid user) role ent]))))

(defn list-entities
  ""
  [] (query! db
        "select distinct(entity) from user_role_entity;"))

(defn assign-role
 ""
 [user role]
 (if-not (have-role? user role) 
   (execute! db
    "insert into user_role_entity (uuid,role) values (?,?)"
    [(:uuid user) role])))

(defn assign-entity
 ""
 [user role entity]
 (if-not (have-access? user role entity)
   (execute! db
    "insert into user_role_entity (uuid,role,entity) values (?,?,?)"
    [(:uuid user) role entity])))

(defn unassign-entity
  ""
  [user role entity]
  (execute! db
   "delete from user_role_entity where uuid=? and role=? and entity=?"
    [(:uuid user) role entity]))


(defn unassign-role
 ""
 [user role]
  (execute! db
   "delete from user_role_entity where uuid=? and role=?"
    [(:uuid user) role]))

(defn assign-tree
  ""
  [user] 
   (let [assigns (query! db "select role, entity from user_role_entity where uuid=?" [(:uuid user)])]
     (for [role (distinct (map :role assigns))]
       (hash-map :role role
                 :entities 
                  (map :entity
                    (filter #(= role (:role %))
                      (filter #(not (nil? (:entity %))) assigns)))))))


(defn user-assignments
  ""
  [user]
  (query! db
    "select role,entity from user_role_entity where uuid =? and entity is not null"
    [(:uuid user)]))

(defn find-role
  ""
  [part] 
   (map :role
     (query! db
      "select distinct( role ) from user_role_entity where role like ?"
      [(str "%" part "%")])))

(defn find-entity
  ""
  [part] 
   (map :entity
     (query! db
      "select distinct( entity ) from user_role_entity where entity like ?"
      [(str "%" part "%")])))

(defn find-users-of-role
  ""
  [role]
   (query! db
    "select * from users where uuid in
      (select uuid from user_role_entity where role=?)"
    [role]))


