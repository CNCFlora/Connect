(ns flora-connect.roles
  (:use flora-connect.db)
  (:use flora-connect.users))

(defn del-role
  ""
  [role]
   (execute! db "delete from roles where role=?"
    [role]))

(defn del-app
  ""
  [app]
   (execute! db "delete from apps where app=?"
    [app]))

(defn add-role
  ""
  [role]
   (execute! db "insert into roles (role) values (?)"
    [role]))

(defn add-app
  ""
  [app]
   (execute! db "insert into apps (app) values (?)"
    [app]))

(defn list-roles
  ""
  [] (map :role (query! db "select role from roles")))

(defn list-apps
  ""
  [] (map :app (query! db "select app from apps") ))

(defn have-role?
  ""
  [user app role] 
  (not
    (empty?
     (query! db
       "select * from user_app_role_entity where uuid = ? and role = ? and app = ? "
         [(:uuid user) role app]))))

(defn have-access?
  ""
  [user app role ent] 
  (not
    (empty?
     (query! db
       "select * from user_app_role_entity where uuid = ? and app = ? and role = ? and entity = ?"
         [(:uuid user) app role ent]))))

(defn list-entities
  ""
  [] (query! db
        "select distinct(entity) from user_app_role_entity;"))

(defn assign-app
 ""
 [user app ]
 (execute! db
  "insert into user_app_role_entity (uuid,app) values (?,?)"
    [(:uuid user) app]))

(defn assign-role
 ""
 [user app role]
 (if-not (have-role? user app role) 
   (execute! db
    "insert into user_app_role_entity (uuid,app,role) values (?,?,?)"
    [(:uuid user) app role])))

(defn assign-entity
 ""
 [user app role entity]
 (if-not (have-access? user app role entity)
   (execute! db
    "insert into user_app_role_entity (uuid,app,role,entity) values (?,?,?,?)"
    [(:uuid user) app role entity])))

(defn unassign-entity
  ""
  [user app role entity]
  (execute! db
   "delete from user_app_role_entity where uuid=? and app =? and role=? and entity=?"
    [(:uuid user) app role entity]))

(defn unassign-app
 ""
 [user app]
  (execute! db
   "delete from user_app_role_entity where uuid=? and app=?"
    [(:uuid user) app]))

(defn unassign-role
 ""
 [user app role]
  (execute! db
   "delete from user_app_role_entity where uuid=? and app=? and role=?"
    [(:uuid user) app role]))

(defn assign-tree
  ""
  [user] 
   (let [assigns (query! db "select app, role, entity from user_app_role_entity where uuid=?" [(:uuid user)])]
    (for [app (distinct (map :app assigns))]
      {:app app
       :roles 
       (let [assigns (filter #(= app (:app %)) assigns)]
        (for [role (distinct (map :role assigns))]
         (hash-map :role role
                   :entities 
                    (map :entity
                      (filter #(= role (:role %))
                        (filter #(not (nil? (:entity %))) assigns))))))})))

(defn user-assignments
  ""
  [user]
  (query! db
    "select app,role,entity from user_app_role_entity where uuid =? and entity is not null"
    [(:uuid user)]))

(defn find-app
  ""
  [part] 
   (map :app
     (query! db
      "select distinct(app) from apps where app like ?"
      [(str "%" part "%")])))

(defn find-role
  ""
  [part] 
   (map :role
     (query! db
      "select distinct( role ) from roles where role like ?"
      [(str "%" part "%")])))

(defn find-entity
  ""
  [part] 
   (map :entity
     (query! db
      "select distinct( entity ) from user_app_role_entity where entity like ?"
      [(str "%" part "%")])))

(defn find-users-of-role
  ""
  [role]
   (query! db
    "select * from users where uuid in
      (select uuid from user_app_role_entity where role=?)"
    [role]))

(defn find-users-of-app
  ""
  [app]
   (query! db
    "select * from users where uuid in
      (select uuid from user_app_role_entity where app=?)"
    [app]))
