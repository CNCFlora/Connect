(ns flora-connect.roles-test
  (:use midje.sweet 
        flora-connect.users
        flora-connect.roles))

(defn to-user 
  [u] (find-by-email (:email u)))

(fact "Can assoc roles with entities to users"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role foo "editor")
      (assign-role foo "editor")
      (assign-role foo "tester")
      (assign-entity foo "editor" "vicia:faba")
      (assign-entity foo "editor" "vicia:alba")
      (assign-entity foo "editor" "vicia:alba")
      (user-assignments foo)
        => [ {:role "editor" :entity "vicia:faba"}
             {:role "editor" :entity "vicia:alba"}]
      (assign-tree foo)
        => [ {:role "editor"
              :entities (list "vicia:faba" "vicia:alba")}
             {:role "tester" :entities (list)} ]
      (unassign-entity foo "editor" "vicia:alba")
      (user-assignments foo)
        => [ {:role "editor" :entity "vicia:faba"}]
      (unassign-role foo "editor")
      (unassign-role foo "tester")
      (user-assignments foo)
        => []
      (delete-user foo))))

(fact "Can partially complete entities"
  (let [foo {:email "foo@bar.com"}]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role foo "editor")
      (assign-entity foo "editor" "vicia:alba")
      (find-entity "vic")
       => ["vicia:alba"]
      (find-entity "faba")
       => []
      (delete-user foo))))

(fact "Can find users by role/entity combination"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role (find-by-email "foo@bar.com") "editor")
      (map :email (find-users-of-role "editor") )
       => (list (:email foo ))
      (delete-user foo))))

(fact "Can work as a ACL"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role  foo "editor")
      (assign-entity foo "editor" "vicia:faba")
      (have-role? nil "editor") => false
      (have-role? foo "editor") => true
      (have-role? foo "coder") => false
      (have-access? foo "editor" "vicia:faba") => true 
      (have-access? foo "editor" "vicia:alba")  => false 
      (have-access? foo "coder" "vicia:alba") => false
      (delete-user foo))))

