(ns flora-connect.users-test
  (:use midje.sweet 
        flora-connect.db
        flora-connect.users))

(connect)

(future-fact "Notify admin on creation of users"
  true => false)

(fact "Can register an user"
  (create-user {:email "foo@bar.com" :name "Diogo"})
  (:email (find-by-email "foo@bar.com")) => "foo@bar.com"
  (:name (find-by-email "foo@bar.com")) => "Diogo"
  (delete-user {:email "foo@bar.com"}))

(fact "Can not create repeated users"
   (let [user {:email "foo@bar.com" }]
     (valid-new-user? user) => true
     (create-user user)
     (valid-new-user? user) => false
     (delete-user user)))

(fact "Can approve user"
   (let [user {:email "foo@bar.com" }]
     (create-user user)
     (approve-user user)
     (:status (find-by-email "foo@bar.com")) => "approved"
     (block-user user)
     (:status (find-by-email "foo@bar.com")) => "blocked"
     (delete-user user)))

(future-fact "Notify user and admin on aproval"
    true => false)

(fact "Can validate an user login"
   (let [user {:email "foo@bar.com" :password "123"}]
     (create-user user)
     (valid-user? user) => false
     (approve-user user)
     (valid-user? user) => true
     (valid-user? (assoc user :password "321")) => false
     (delete-user user)))

(fact "Can update user"
  (create-user {:email "diogo" :password "1"})
  (approve-user (find-by-email "diogo"))
  (valid-user? {:email "diogo" :password "123"}) => false
  (valid-user? {:email "diogo" :password "1"}) => true
  (update-user (assoc (find-by-email "diogo") :name "didi"))
  (:name (find-by-email "diogo"))
      => "didi"
  (update-pass (assoc (find-by-email "diogo") :password "123"))
  (valid-user? {:email "diogo" :password "123"}) => true
  (update-pass (assoc (find-by-email "diogo") :password "321"))
  (valid-user? {:email "diogo" :password "321"}) => true
  (valid-user? {:email "diogo" :password "123"}) => false
  (delete-user (find-by-email "diogo")))

