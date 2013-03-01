(ns cncflora-connect.roles-test
  (:use midje.sweet 
        cncflora-connect.users
        cncflora-connect.roles))

(fact "Can work with roles"
  (register-role "editor")
  (register-role "editor")
  (register-role "programer")
  (register-role "wrong")
  (remove-role "wrong")
  (list-roles) => (list "editor" "programer" ))

(fact "Can work with entities"
  (register-entity {:name "Vicia faba" :value "vicia:faba"})
  (register-entity {:name "Vicia nao repete" :value "vicia:faba"})
  (register-entity {:name "Vicia alba" :value "vicia:alba"})
  (remove-entity "vicia:alba")
  (map :value (list-entities) ) => (list "vicia:faba"))

(fact "Can assoc roles with entities to users"
  (let [foo {:email "foo@bar.com" :password "123"}]
    (register-role "editor")
    (register-entity {:name "Vicia faba" :value "vicia:faba"})
    (register-entity {:name "Vicia alba" :value "vicia:alba"})
    (create-user foo)
    (assign-role foo "editor")
    (assign-entity foo "editor" "vicia:faba")
    (assign-entity foo "editor" "vicia:alba")
    (user-assignments foo)
      => [ {:role "editor" :entity "vicia:faba"}
           {:role "editor" :entity "vicia:alba"}]
    (unassign-entity foo "editor" "vicia:alba")
    (user-assignments foo)
      => [ {:role "editor" :entity "vicia:faba"}]
    (unassign-role foo "editor")
    (user-assignments foo)
      => []
    (remove-role "editor")
    (remove-entity "vicia:faba")
    (delete-user foo)))

(fact "The removal of a role or an entity cascades to users"
  (let [foo {:email "foo@bar.com" :password "123"}]
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
    ))

(fact "Can find users by role/entity combination"
  true => false)

(fact "Can work as a ACL"
  true => false)

