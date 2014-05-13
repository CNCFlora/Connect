(ns flora-connect.search
  (:use flora-connect.db)
  (:use flora-connect.users)
  (:use flora-connect.roles))

(defn search [n]
 (filter #(not (nil? %))
   (map #(find-by-uuid (:uuid %))
     (distinct 
       (flatten
         (merge
           (query! db
            "select distinct( uuid ) from users where email like ?"
             [(str "%" n "%")])
           (query! db
            "select distinct( uuid ) from users_data where name like ?"
             [(str "%" n "%")])
           (query! db
            "select distinct( uuid ) from user_role_entity where role like ? or entity like ?"
             [(str "%" n "%") (str "%" n "%")])))))))

