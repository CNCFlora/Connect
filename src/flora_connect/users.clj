(ns flora-connect.users
  (:use flora-connect.db))

(defn sha1
  ""
  [obj]
  (.toString 
    (new java.math.BigInteger 1
     (.digest (java.security.MessageDigest/getInstance "SHA1")
              (.getBytes (with-out-str (pr obj))))) 16))

(defn notify
  "Send an e-mail notification to some user."
  [who what] what)

(defn notify-creation
  "Notify responsable users the creation of another user."
  [user] user)

(defn notify-approval
  "Notify responsable users the approval of another user."
  [user] user)

(defn uuid 
  "Generate some random uuid"
  [] (str (java.util.UUID/randomUUID)))

(defn valid-new-user?
  "Validate if an user ok to be created.
   Validates: unique e-mail."
  [user]
  (or
    (nil? (:email user))
    (let [r (get! db :users :email (:email user))]
      (empty? r))))

(defn create-user 
  "Create a new user with default stuff."
  [user] 
    (if (valid-new-user? user)
      (let [main {:uuid (uuid) :name (:name user) :email (:email user) :password (sha1 (:password user)) :status "waiting"}
            data (assoc (dissoc user :email :password) :uuid (:uuid main)) ]
        (create! db :users main)
        (notify-creation user))))

(defn approve-user
  "Aprove an user registral"
  [user]
   (execute! db
     "UPDATE users SET status=? WHERE email=?"
      ["approved" (:email user)]))

(defn block-user
  "Block an user account"
  [user]
   (execute! db
     "UPDATE users SET status=? WHERE email=?"
      ["blocked" (:email user)]))

(defn get-users
  "Return all users or paginated" 
  ([] (query! db "select * from users"))
  ([page] 
    (query! db (str "SELECT * FROM users ORDER BY name OFFSET " (* page 20) " ROWS FETCH NEXT 20 ROWS ONLY "))))

(defn valid-user?
  "Check a login validity, including approval status"
  [user]
  (let [r (get! db :users :email (:email user))]
    (if (empty? r)
      false
      (and
        (= (sha1 (:password user)) (:password (first r)))
        (= "approved" (:status (first r)))))))

(defn user
  ""
  [user]
 (if-not (nil? user)
    (merge user 
       {
        :is_approved (= "approved" (:status user))
        :is_blocked (= "blocked" (:status user))
        :is_waiting (= "waiting" (:status user))
       })))

(defn find-by-uuid
  "Find an user by uuid"
  [uuid] 
   (user (first (get! db :users :uuid uuid))))

(defn find-by-email
  "Find an user by email"
  [email] 
   (user (first (get! db :users :email email))))

(defn delete-user
  "Deletes an user. Internal only."
  [user]
  (let [user (first (get! db :users :email (:email user)))]
    (execute! db "DELETE FROM user_role_entity WHERE uuid = ?" [(:uuid user)])
    (execute! db "DELETE FROM users WHERE uuid = ?" [(:uuid user)])))

(defn update-user
  ""
  [user]
  (execute! db
   (str "UPDATE users SET"
        " name=?,"
        " email=?,"
        " status=?"
        " WHERE uuid=? ")
    [(:name user) (:email user) (:status user) (:uuid user)]))

(defn update-pass
  ""
  [user]
  (execute! db
    "UPDATE users SET passoword=? where uuid=?"
      [(sha1 (:password user)) (:uuid user)]))

(defn have-admin?
  ""
  [] 
   (not (empty? (query! db "select * from user_role_entity where role='admin'"))))

(defn get-pendding
  ""
  [] 
   (query! db
     "SELECT * FROM users WHERE status = ? ORDER BY name" ["waiting"]))

