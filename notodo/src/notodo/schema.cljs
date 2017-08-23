(ns notodo.schema
  (:require [notodo.db :as db]
            [re-frame.core :as rf]
            [cljs.spec.alpha :as s]))

(defn persistent-tree-map? [x]
  (instance? PersistentTreeMap x))

(s/def ::db/id (s/and int? #(>= % 0)))

(s/def ::db/next-id ::db/id)
(s/def ::db/edit (s/nilable ::db/id))

(s/def ::db/created-at inst?)
(s/def ::db/content (s/nilable string?))

(s/def ::db/todo
  (s/keys :req [::db/id ::db/created-at]
          :opt [::db/content]))

(s/def ::db/todos
  (s/and (s/map-of ::db/id ::db/todo)
         persistent-tree-map?))

(s/def ::db/db
  (s/keys :req [::db/next-id]
          :opt [::db/todos ::db/edit]))

(def check-specs-interceptor
  (fn [spec]
    (rf/after (fn [db]
                (when-not (s/valid? spec db)
                  (let [message (s/explain-str spec db)
                        data (s/explain-data spec db)]
                    (throw (ex-info message data))))))))

