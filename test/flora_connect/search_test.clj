(ns flora-connect.search-test
  (:use midje.sweet 
        flora-connect.users
        flora-connect.roles
        flora-connect.search))

(fact "Can search"
  (let [john {:email "jbar@foo.com" :name "John Doe"}
        earl {:email "earl@foo.com" :name "Earl"}]
    (create-user john)
    (create-user earl)
    (let [john (find-by-email (:email john))
          earl (find-by-email (:email earl))]
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
      (map :email (search "vicia"))
       => (list "jbar@foo.com" "earl@foo.com")
      (delete-user john)
      (delete-user earl))))

