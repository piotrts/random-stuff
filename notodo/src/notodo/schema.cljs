(ns notodo.schema
  (:require [notodo.db :as db]
            [re-frame.core :as rf]
            [cljs.spec.alpha :as s]))

(s/def ::db/next-id (s/and int? (complement neg?)))

(s/def ::db/id (s/and int? (complement neg?)))
(s/def ::db/created-at inst?)
(s/def ::db/content (s/nilable string?))
(s/def ::db/editing? (s/nilable boolean?))

(s/def ::db/todo
  (s/keys :req [::db/id ::db/created-at]
          :opt [::db/content ::db/editing?]))

(s/def ::db/todos
  (s/map-of ::db/id ::db/todo))

(s/def ::db/db
  (s/keys :req [::db/next-id]
          :opt [::db/todos]))

(def check-specs
  (rf/after (fn [db]
              (when-not (s/valid? ::db/db db)
                (throw (ex-info (s/explain-str ::db/db db) {}))))))
