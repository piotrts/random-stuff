(ns notodo.schema
  (:require [notodo.db :as db]
            [re-frame.core :as rf]
            [cljs.spec.alpha :as s]))

(defn >=0? [x]
  (>= x 0))

(s/def ::db/next-id (s/and int? >=0?))
(s/def ::db/edit (s/nilable >=0?))

(s/def ::db/id (s/and int? >=0?))
(s/def ::db/created-at inst?)
(s/def ::db/content (s/nilable string?))

(s/def ::db/todo
  (s/keys :req [::db/id ::db/created-at]
          :opt [::db/content]))

(s/def ::db/todos
  (s/map-of ::db/id ::db/todo))

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

