(ns cncflora-connect.roles-test
  (:use midje.sweet 
        cncflora-connect.users
        cncflora-connect.roles))

(connect "testdata")

(fact "Can work with roles"
  (register-role "admin")
  (register-role "editor")
  (register-role "editor")
  (register-role "programer")
  (register-role "wrong")
  (remove-role "wrong") 
  (sort (vec (list-roles) ) )  => (sort (vector "admin" "programer" "editor" ) )
  (remove-role "admin")
  (sort (vec (list-roles) ) ) => (sort (vector "admin" "programer" "editor" ) ))

(fact "Can work with entities"
  (register-entity {:name "Vicia faba" :value "vicia:faba"})
  (register-entity {:name "Vicia nao repete" :value "vicia:faba"})
  (register-entity {:name "Vicia alba" :value "vicia:alba"})
  (remove-entity "vicia:alba")
  (map :value (list-entities) ) => (list "vicia:faba"))

(fact "Can assoc roles with entities to users"
  (let [foo {:email "foo@bar.com" }]
    (register-role "editor")
    (register-entity {:name "Vicia faba" :value "vicia:faba"})
    (register-entity {:name "Vicia alba" :value "vicia:alba"})
    (create-user foo)
    (assign-role foo "editor")
    (assign-role foo "editor")
    (assign-entity foo "editor" "vicia:faba")
    (assign-entity foo "editor" "vicia:alba")
    (assign-entity foo "editor" "vicia:alba")
    (user-assignments foo)
      => [ {:role "editor" :entity "vicia:faba"}
           {:role "editor" :entity "vicia:alba"}]
    (assign-tree foo)
      => [ {:role "editor"
            :entities (list 
                        {:name "Vicia faba" :value "vicia:faba"}
                        {:name "Vicia alba" :value "vicia:alba"})}]
    (unassign-entity foo "editor" "vicia:alba")
    (user-assignments foo)
      => [ {:role "editor" :entity "vicia:faba"}]
    (unassign-role foo "editor")
    (user-assignments foo)
      => []
    (remove-role "editor")
    (remove-entity "vicia:faba")
    (remove-entity "vicia:alba")
    (delete-user foo)))

(fact "The removal of a role or an entity cascades to users"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (register-role "editor")
    (register-entity {:name "Vicia faba" :value "vicia:faba"})
    (register-entity {:name "Vicia alba" :value "vicia:alba"})
    (assign-role foo "editor")
    (assign-entity foo "editor" "vicia:faba")
    (assign-entity foo "editor" "vicia:alba")
    (remove-entity "vicia:alba")
    (user-assignments foo)
      => [ {:role "editor" :entity "vicia:faba"}]
    (remove-role "editor")
    (user-assignments foo)
      => []
    (delete-user foo)))

(fact "Can partially complete entities"
  (let [ent {:name "Vicia faba" :value "vicia:faba"}]
    (register-entity ent)
    (find-entity "Vic")
     => [{:name "Vicia faba" :value "vicia:faba" :type "entity"}]
    (find-entity "fab")
     => [{:name "Vicia faba" :value "vicia:faba" :type "entity"}]
    (find-entity "alba")
     => []
    (remove-entity "vicia:faba")))

(fact "Can find users by role/entity combination"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (register-role "editor")
    (assign-role foo "editor")
    (map :email (find-users-of-role "editor") )
     => (list (:email foo ))
    (remove-role "editor")
    (delete-user foo)))

(fact "Can work as a ACL"
  (let [foo {:email "foo@bar.com" }]
    (create-user foo)
    (register-role "editor")
    (register-role "coder")
    (register-entity {:name "Vicia faba" :value "vicia:faba"})
    (register-entity {:name "Vicia alba" :value "vicia:alba"})
    (assign-role foo "editor")
    (assign-entity foo "editor" "vicia:faba")
    (have-role? nil "editor") => false
    (have-role? foo "editor") => true
    (have-role? foo "coder") => false
    (have-access? foo "editor" "vicia:faba") => true 
    (have-access? foo "editor" "vicia:alba")  => false 
    (have-access? foo "coder" "vicia:alba") => false
    (remove-role "editor")
    (remove-role "coder")
    (remove-entity "vicia:faba")
    (remove-entity "vicia:alba")
    (delete-user foo)))


