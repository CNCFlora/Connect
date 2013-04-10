(ns cncflora-connect.search-test
  (:use midje.sweet 
        cncflora-connect.users
        cncflora-connect.roles
        cncflora-connect.search))

(connect "testdata")

(fact "Can search"
  (let [john {:email "jbar@foo.com" :name "John Doe"}
        earl {:email "earl@foo.com" :name "Earl"}]
    (create-user john)
    (create-user earl)
    (register-role "editor")
    (register-role "reader")
    (register-entity {:name "Vicia faba" :value "vicia:faba"})
    (register-entity {:name "Vicia alba" :value "vicia:alba"})
    (assign-role john "editor")
    (assign-role earl "reader")
    (assign-entity john "editor" "vicia:faba")
    (assign-entity earl "reader" "vicia:alba")
    (map :email (search "John"))
     => (list "jbar@foo.com")
    (map :email (search "editor"))
     => (list "jbar@foo.com")
    (map :email (search "reader"))
     => (list "earl@foo.com")
    (map :email (search "faba"))
     => (list "jbar@foo.com")
    (map :email (search "alba"))
     => (list "earl@foo.com")
    (map :email (search "Vicia"))
     => (list "jbar@foo.com" "earl@foo.com")
    (remove-role "editor")
    (remove-role "reader")
    (remove-entity "vicia:faba")
    (remove-entity "vicia:alba")
    (delete-user john)))
