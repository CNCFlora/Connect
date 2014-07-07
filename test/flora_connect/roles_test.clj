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
      (assign-role foo "app1" "editor")
      (assign-role foo "app1" "editor")
      (assign-role foo "app1" "tester")
      (assign-entity foo "app1" "editor" "vicia:faba")
      (assign-entity foo "app1" "editor" "vicia:alba")
      (assign-entity foo "app1" "editor" "vicia:alba")
      (assign-entity foo "app2" "editor" "vicia:alba")
      (user-assignments foo)
        => [ {:role "editor" :app "app1" :entity "vicia:faba"}
             {:role "editor" :app "app1" :entity "vicia:alba"}
             {:role "editor" :app "app2" :entity "vicia:alba"} ]
      (assign-tree foo)
        => (list
            {:app "app1"
              :roles [ {:role "editor" :entities (list "vicia:faba" "vicia:alba")}
                       {:role "tester" :entities (list)} ]}
            {:app "app2"
              :roles [ {:role "editor" :entities (list "vicia:alba")} ]})
      (unassign-entity foo "app1" "editor" "vicia:alba")
      (unassign-entity foo "app2" "editor" "vicia:alba")
      (user-assignments foo)
        => [ {:role "editor" :app "app1" :entity "vicia:faba"}]
      (unassign-role foo "app1" "editor")
      (unassign-role foo "app2" "editor")
      (unassign-role foo "app1" "tester")
      (user-assignments foo)
        => []
      (delete-user foo))))

(fact "Can partially complete entities"
  (let [foo {:email "foo@bar.com"}]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role foo "app1" "editor")
      (assign-entity foo "app1" "editor" "vicia:alba")
      (find-entity "vic")
       => ["vicia:alba"]
      (find-entity "faba")
       => []
      (delete-user foo))))

(fact "Can find users by role/entity combination"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role (find-by-email "foo@bar.com") "app1" "editor")
      (map :email (find-users-of-role "editor") )
       => (list (:email foo ))
      (delete-user foo))))

(fact "Can work as a ACL"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role  foo "app1" "editor")
      (assign-entity foo "app1" "editor" "vicia:faba")
      (assign-entity foo "app2" "editor" "vicia:alba")
      (have-role? nil "app1" "editor") => false
      (have-role? foo "app1" "editor") => true
      (have-role? foo "app2" "editor") => true
      (have-role? foo "app1" "coder") => false
      (have-access? foo "app1 ""editor" "vicia:faba") => true 
      (have-access? foo  "app1" "editor" "vicia:alba")  => false 
      (have-access? foo  "app2" "editor" "vicia:alba")  => true
      (have-access? foo "app1" "coder" "vicia:alba") => false
      (delete-user foo))))

(fact "Find role && entity && app"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (let [foo (to-user foo)]
      (assign-role foo "app1" "editor")
      (assign-entity foo "app1" "editor" "vicia")
      (find-role "ed") => ["editor"]
      (find-entity "ici") => ["vicia"]
      (find-app "pp") => ["app1"]
      (delete-user foo)
      )))

