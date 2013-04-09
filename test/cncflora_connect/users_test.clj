(ns cncflora-connect.users-test
  (:use midje.sweet 
        cncflora-connect.users))

(connect "testdata")

(fact "Notify admin on creation of users"
  true => false)

(fact "Can register an user"
  (create-user {:email "foo@bar.com"})
  (:email (find-by-email "foo@bar.com")) => "foo@bar.com"
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

(fact "Notify user and admin on aproval"
    true => false)

(fact "Can validate an user login"
   (let [user {:email "foo@bar.com"}]
     (create-user user)
     (valid-user? user) => false
     (approve-user user)
     (valid-user? user) => true
     (delete-user user)))

(fact "Can update user"
  (create-user {:email "diogo"})
  (update-user (assoc (find-by-email "diogo") :name "didi"))
  (:name (find-by-email "diogo"))
      => "didi"
  (delete-user (find-by-email "diogo")))

(fact "Can search user"
  (create-user {:email "dio@me.com" :name "marco"})
  (map :name (search-users "ma"))
      => (list "marco")
  (map :name (search-users "arc"))
      => (list "marco")
  (search-users "ju")
      => (list )
  (delete-user (find-by-email "dio@me.com")))
