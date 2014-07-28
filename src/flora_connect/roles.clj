(ns flora-connect.roles
  (:use flora-connect.db)
  (:use flora-connect.users))

(defn del-role
  ""
  [role]
   (execute! db "delete from roles where role=?"
    [role]))

(defn del-context
  ""
  [context]
   (execute! db "delete from contexts where context=?"
    [context]))

(defn add-role
  ""
  [role]
   (execute! db "insert into roles (role) values (?)"
    [role]))

(defn add-context
  ""
  [context]
   (execute! db "insert into contexts (context) values (?)"
    [context]))

(defn list-roles
  ""
  [] (map :role (query! db "select role from roles")))

(defn list-contexts
  ""
  [] (map :context (query! db "select context from contexts") ))

(defn have-role?
  ""
  [user context role] 
  (not
    (empty?
     (query! db
       "select * from user_context_role_entity where uuid = ? and role = ? and context = ? "
         [(:uuid user) role context]))))

(defn have-access?
  ""
  [user context role ent] 
  (not
    (empty?
     (query! db
       "select * from user_context_role_entity where uuid = ? and context = ? and role = ? and entity = ?"
         [(:uuid user) context role ent]))))

(defn list-entities
  ""
  [] (query! db
        "select distinct(entity) from user_context_role_entity;"))

(defn assign-context
 ""
 [user context ]
 (execute! db
  "insert into user_context_role_entity (uuid,context) values (?,?)"
    [(:uuid user) context]))

(defn assign-role
 ""
 [user context role]
 (if-not (have-role? user context role) 
   (execute! db
    "insert into user_context_role_entity (uuid,context,role) values (?,?,?)"
    [(:uuid user) context role])))

(defn assign-entity
 ""
 [user context role entity]
 (if-not (have-access? user context role entity)
   (execute! db
    "insert into user_context_role_entity (uuid,context,role,entity) values (?,?,?,?)"
    [(:uuid user) context role entity])))

(defn unassign-entity
  ""
  [user context role entity]
  (execute! db
   "delete from user_context_role_entity where uuid=? and context =? and role=? and entity=?"
    [(:uuid user) context role entity]))

(defn unassign-context
 ""
 [user context]
  (execute! db
   "delete from user_context_role_entity where uuid=? and context=?"
    [(:uuid user) context]))

(defn unassign-role
 ""
 [user context role]
  (execute! db
   "delete from user_context_role_entity where uuid=? and context=? and role=?"
    [(:uuid user) context role]))

(defn assign-tree
  ""
  ([user] 
   (let [assigns (query! db "select context, role, entity from user_context_role_entity where uuid=?" [(:uuid user)])]
    (for [context (distinct (map :context assigns))]
      {:context context
       :roles 
       (let [assigns (filter #(= context (:context %)) assigns)]
        (for [role (distinct (map :role assigns))]
         (hash-map :role role
                   :entities 
                    (map :entity
                      (filter #(= role (:role %))
                        (filter #(not (nil? (:entity %))) assigns))))))})))
   ([user ctx] 
    (if-let [roles (:roles (first (filter #(= ctx (:context %)) (assign-tree user))))]
      roles
      []
      )
    ))

(defn user-assignments
  ""
  [user]
  (query! db
    "select context,role,entity from user_context_role_entity where uuid =? and entity is not null"
    [(:uuid user)]))

(defn find-context
  ""
  [part] 
   (map :context
     (query! db
      "select distinct(context) from contexts where context like ?"
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
      "select distinct( entity ) from user_context_role_entity where entity like ?"
      [(str "%" part "%")])))

(defn find-users-of-role
  ""
  [role]
   (query! db
    "select * from users where uuid in
      (select uuid from user_context_role_entity where role=?)"
    [role]))

(defn find-users-of-context
  ""
  [context]
   (query! db
    "select * from users where uuid in
      (select uuid from user_context_role_entity where context=?)"
    [context]))
