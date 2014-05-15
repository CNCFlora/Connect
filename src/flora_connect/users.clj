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
      (let [main {:uuid (uuid) :email (:email user) :password (sha1 (:password user)) :status "waiting"}
            data (assoc (dissoc user :email :password) :uuid (:uuid main))]
        (create! db :users main)
        (create! db :users_data data)
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
  ([] (query! db "select * from users inner join users_data on users_data.uuid = users.uuid"))
  ([page] 
    (query! db ("select * from users inner join users_data on users_data.uuid = users.uuid START  " (* page 20) "  LIMIT 20"))))

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
   (let [data (first (get! db :users_data :uuid (:uuid user)))]
    (merge user data
       {
        :is_approved (= "approved" (:status user))
        :is_blocked (= "blocked" (:status user))
        :is_waiting (= "waiting" (:status user))
       }))))

(defn find-by-uuid
  "Find an user by uuid"
  [uuid] 
   (user (first (get! db :users :uuid uuid))))

(defn find-by-email
  "Find an user by email"
  [email] 
   (user (first (get! db :users :email email))) )

(defn delete-user
  "Deletes an user. Internal only."
  [user]
  (delete! db :users (first (get! db :users :email (:email user)))))

(defn update-user
  ""
  [user]
  (execute! db
   (str "UPDATE users SET"
        " email=?,"
        " status=?"
        " WHERE uuid=? ")
    [(:email user) (:status user) (:uuid user)])
  (execute! db
   (str "UPDATE users_data SET"
        " name=?,"
        " institute=?,"
        " phone=?,"
        " address=?,"
        " postal=?,"
        " state=?,"
        " city=?,"
        " complement=?"
        " WHERE uuid=? ")
    [(:name user) (:institude user) (:phone user) 
     (:address user) (:postal user) (:state user) 
     (:city user) (:complement user) (:uuid user)]))

(defn update-pass
  ""
  [user]
  (execute! db
    "UPDATE users SET passoword=? where uuid=?"
          [(sha1 (:password user)) (:uuid user)]))

(defn have-admin?
  ""
  [] 
   (not (empty? (query! db "select * from user_roles_entity where role='admin'"))))

(defn get-pendding
  ""
  [] 
   (query! db
     "SELECT * FROM users WHERE status = ? inner join users_data on users_data.uuid = users.uuid ORDER BY name"))

