(ns cncflora-connect.roles-test
  (:use midje.sweet 
        cncflora-connect.roles))

(fact "Can work with roles"
  (register-role "editor")
  (register-role "editor")
  (register-role "programer")
  (register-role "wrong")
  (remove-role "wrong")
  (list-roles) => (list "programer""editor" ))

(fact "Can work with entities"
  (register-entity {:name "Vicia faba" :value "vicia:faba"})
  (register-entity {:name "Vicia nao repete" :value "vicia:faba"})
  (register-entity {:name "Vicia alba" :value "vicia:alba"})
  (remove-entity "vicia:alba")
  (map :value (list-entities) ) => (list "vicia:faba"))

(fact "The removal of a role or an entity cascades to users"
  true => false)

(fact "Can partially completes entities"
  true => false)

(fact "Can find users by role/entity combination"
  true => false)

(fact "Can work as a ACL"
  true => false)
