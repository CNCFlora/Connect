(ns cncflora-connect.users
  (:use simple-cypher.core))

(def db (graph "data"))

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

(defn enc
  "Encrypts a password"
  [s] s)

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
                      :status "waiting"
                      :password (enc (:password user)))
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
  "Return all users" 
  [] (map :users
      (query! db 
        "START users=node:nodes(type='user') return users")))

(defn valid-user?
  "Check a login validity, including approval status"
  [user]
  (let [r (query! db (str "START user=node:nodes(email='" (:email user) "') RETURN user"))]
    (if (empty? r)
      false
      (and
        (= (enc (:password user))
           (:password (:user (first r))))
        (= "approved" (:status (:user (first r))))))))

(defn find-by-uuid
  "Find an user by uuid"
  [uuid] 
  (let [r (query! db (str "START user=node:nodes(uuid='" uuid "') RETURN user"))]
    (:user (first r))))

(defn find-by-email
  "Find an user by email"
  [email] 
  (let [r (query! db (str "START user=node:nodes(email='" email "') RETURN user"))]
    (:user (first r))))

(defn delete-user
  "Deletes an user. Internal only."
  [user]
  (delete! db (first (get! db :email (:email user) :raw))))

(defn have-admin?
  ""
  [] 
  (not (empty? (get! db :role "admin"))))

(defn pendding
  ""
  [] (map :u (query! db 
      (str "START u=node:nodes(status='waiting') WHERE u.status = 'waiting' RETURN u"))))
