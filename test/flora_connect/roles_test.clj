(ns flora-connect.roles-test
  (:use midje.sweet 
        flora-connect.db
        flora-connect.users
        flora-connect.roles))

(connect)

(defn to-user 
  [u] (find-by-email (:email u)))

(fact "Can assoc roles with entities to users"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role foo "context1" "editor")
      (assign-role foo "context1" "editor")
      (assign-role foo "context1" "tester")
      (assign-entity foo "context1" "editor" "vicia:faba")
      (assign-entity foo "context1" "editor" "vicia:alba")
      (assign-entity foo "context1" "editor" "vicia:alba")
      (assign-entity foo "context2" "editor" "vicia:alba")
      (user-assignments foo)
        => [ {:role "editor" :context "context1" :entity "vicia:faba"}
             {:role "editor" :context "context1" :entity "vicia:alba"}
             {:role "editor" :context "context2" :entity "vicia:alba"} ]
      (assign-tree foo)
        => (list
            {:context "context1"
              :roles [ {:role "editor" :entities (list "vicia:faba" "vicia:alba")}
                       {:role "tester" :entities (list)} ]}
            {:context "context2"
              :roles [ {:role "editor" :entities (list "vicia:alba")} ]})
      (unassign-entity foo "context1" "editor" "vicia:alba")
      (unassign-entity foo "context2" "editor" "vicia:alba")
      (user-assignments foo)
        => [ {:role "editor" :context "context1" :entity "vicia:faba"}]
      (unassign-role foo "context1" "editor")
      (unassign-role foo "context2" "editor")
      (unassign-role foo "context1" "tester")
      (user-assignments foo)
        => []
      (delete-user foo))))

(fact "Can partially complete entities"
  (let [foo {:email "foo@bar.com"}]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role foo "context1" "editor")
      (assign-entity foo "context1" "editor" "vicia:alba")
      (find-entity "vic")
       => ["vicia:alba"]
      (find-entity "faba")
       => []
      (delete-user foo))))

(fact "Can find users by role/entity combination"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role (find-by-email "foo@bar.com") "context1" "editor")
      (map :email (find-users-of-role "editor") )
       => (list (:email foo ))
      (delete-user foo))))

(fact "Can work as a ACL"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role  foo "context1" "editor")
      (assign-entity foo "context1" "editor" "vicia:faba")
      (assign-entity foo "context2" "editor" "vicia:alba")
      (have-role? nil "context1" "editor") => false
      (have-role? foo "context1" "editor") => true
      (have-role? foo "context2" "editor") => true
      (have-role? foo "context1" "coder") => false
      (have-access? foo "context1 ""editor" "vicia:faba") => true 
      (have-access? foo  "context1" "editor" "vicia:alba")  => false 
      (have-access? foo  "context2" "editor" "vicia:alba")  => true
      (have-access? foo "context1" "coder" "vicia:alba") => false
      (delete-user foo))))

(fact "Find role && entity && context"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (add-role "editor")
      (add-context "context1")
      (assign-role foo "context1" "editor")
      (assign-entity foo "context1" "editor" "vicia")
      (find-role "ed") => ["editor"]
      (find-entity "ici") => ["vicia"]
      (find-context "ext") => ["context1"]
      (delete-user foo)
      )))

