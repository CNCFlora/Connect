(ns cncflora-connect.users
  (:use simple-cypher.core))

(declare db)

(defn connect
  ""
  [store] (def db (graph store)))

(defn notify
  "Send an e-mail notification to some user."
  [who what] nil)

(defn notify-creation
  "Notify responsable users the creation of another user."
  [user] nil)

(defn notify-approval
  "Notify responsable users the approval of another user."
  [user] nil)

(defn uuid 
  "Generate some random uuid"
  [] (str (java.util.UUID/randomUUID)))

(defn valid-new-user?
  "Validate if an user ok to be created.
   Validates: unique e-mail."
  [user]
  (let [r (get! db :email (:email user))]
    (empty? r)))

(defn create-user 
  "Create a new user with default stuff."
  [user] 
    (if (valid-new-user? user)
      (create! db 
          (assoc user :type "user"
                      :uuid (uuid)
                      :status "waiting")
           :index)
         (notify-creation user)))

(defn approve-user
  "Aprove an user registral"
  [user]
   (query! db
     (str "START user=node:nodes(email='" (:email user) "')"
          " SET user.status = 'approved'")))

(defn block-user
  "Block an user account"
  [user]
   (query! db
     (str "START user=node:nodes(email='" (:email user) "')"
          " SET user.status = 'blocked'")))

(defn get-users
  "Return all users or paginated" 
  ([] (map :users
       (query! db 
        "START users=node:nodes(type='user') return users")))
  ([page] (map :users
       (query! db 
        (str "START users=node:nodes(type='user')"
             " RETURN users"
             " SKIP " (* page 20) " LIMIT 20")))))

(defn valid-user?
  "Check a login validity, including approval status"
  [user]
  (let [r (query! db (str "START user=node:nodes(email='" (:email user) "') RETURN user"))]
    (if (empty? r)
      false
      (and
        (= "approved" (:status (:user (first r))))))))

(defn user
  ""
  [user]
  (assoc user
    :is_approved (= "approved" (:status user))
    :is_blocked (= "blocked" (:status user))
    :is_waiting (= "waiting" (:status user))))

(defn find-by-uuid
  "Find an user by uuid"
  [uuid] 
  (let [r (query! db (str "START user=node:nodes(uuid='" uuid "') RETURN user"))]
    (user (:user (first r)) )))

(defn find-by-email
  "Find an user by email"
  [email] 
  (let [r (query! db (str "START user=node:nodes(email='" email "') RETURN user"))]
    (:user (first r))))

(defn delete-user
  "Deletes an user. Internal only."
  [user]
  (delete! db (first (get! db :email (:email user) :raw))))

(defn update-user
  ""
  [user]
  (query! db
   (str "START u=node:nodes(uuid='" (:uuid user ) "')"
        " SET u.email  = '" (:email user) "'"
        " SET u.name   = '" (:name user) "'"
        " SET u.status = '" (:status user) "'")))

(defn have-admin?
  ""
  [] 
  (not (empty? (get! db :role "admin"))))

(defn get-pendding
  ""
  [] (map :u (query! db 
      (str "START u=node:nodes(status='waiting') WHERE u.status = 'waiting' RETURN u"))))

(defn search-users
  ""
  [q] 
  (map :u
   (query! db
    (str "START u=node:nodes(\"name:*" q "*\")"
         " RETURN u"))) )

