(ns cncflora-connect.users-test
  (:use midje.sweet 
        cncflora-connect.users))

(fact "Notify admin on creation of users"
  true => false)

(fact "Can register an user"
  (create-user {:email "foo@bar.com" :password "123"})
  (:email (find-by-email "foo@bar.com")) => "foo@bar.com"
  (delete-user {:email "foo@bar.com"}))

(fact "Can not create repeated users"
   (let [user {:email "foo@bar.com" :password "123"}]
     (valid-new-user? user) => true
     (create-user user)
     (valid-new-user? user) => false
     (delete-user user)))

(fact "Can approve user"
   (let [user {:email "foo@bar.com" :password "123"}]
     (create-user user)
     (approve-user user)
     (:status (find-by-email "foo@bar.com")) => "approved"
     (block-user user)
     (:status (find-by-email "foo@bar.com")) => "blocked"
     (delete-user user)))

(fact "Notify user and admin on aproval"
    true => false)

(fact "Can validate an user login"
   (let [user {:email "foo@bar.com" :password "123"}]
     (create-user user)
     (valid-user? user) => false
     (approve-user user)
     (valid-user? user) => true
     (valid-user? {:email "foo@bar.com" :password "321"}) => false
     (block-user user)
     (valid-user? user) => false
     (delete-user user)))

