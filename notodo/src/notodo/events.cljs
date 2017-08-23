(ns notodo.events
  (:require [re-frame.core :as rf]
            [cljs.spec.alpha :as s]))

(s/def :notodo.db/next-id (s/and int? (complement neg?)))

(s/def :notodo.db/id (s/and int? (complement neg?)))
(s/def :notodo.db/created-at inst?)
(s/def :notodo.db/content (s/nilable string?))
(s/def :notodo.db/editing? (s/nilable boolean?))

(s/def :notodo.db/todo
  (s/keys :req [:notodo.db/id :notodo.db/created-at]
          :opt [:notodo.db/content :notodo.db/editing?]))

(s/def :notodo.db/todos
  (s/map-of :notodo.db/id :notodo.db/todo))

(s/def :notodo.db/db
  (s/keys :req [:notodo.db/next-id]
          :opt [:notodo.db/todos]))

(def check-specs
  (rf/after (fn [db]
              (when-not (s/valid? :notodo.db/db db)
                (throw (ex-info (s/explain-str :notodo.db/db db) {}))))))

(def interceptors [check-specs])

(rf/reg-event-db ::initialise-db interceptors
  (fn [_ _]
    {:notodo.db/todos (sorted-map-by >)
     :notodo.db/next-id 0}))

(rf/reg-sub ::get-todos
  (fn [db _]
    (vals (:notodo.db/todos db))))

(rf/reg-sub ::get-todo
  (fn [db id]
    (-> db :notodo.db/todos id)))

(rf/reg-event-db ::add-todo interceptors
  (fn [db [_ content opts]]
    (let [id (:notodo.db/next-id db)
          editing? (:notodo.db/editing? opts)
          todo #:notodo.db{:id id
                           :created-at (js/Date.)
                           :content content
                           :editing? editing?}]
      (-> db
        (update :notodo.db/todos assoc id todo)
        (update :notodo.db/next-id inc)))))

(rf/reg-sub ::get-property
  (fn [db [id key]]
    (get-in db [:notodo.db/todos id key])))

(rf/reg-event-db ::set-property interceptors
  (fn [db [_ id key val]]
    (assoc-in db [:notodo.db/todos id key] val)))


(rf/reg-event-db ::set-property-all interceptors
  (fn [db [_ key val]]
    (let [old-todos (:notodo.db/todos db)
          new-todos (into (empty old-todos)
                          (mapv (fn [[idx m]]
                                  [idx (assoc m key val)])
                                old-todos))]
      (assoc db :notodo.db/todos new-todos))))

(rf/reg-event-db ::delete-todo interceptors
  (fn [db [_ id]]
    (update db :notodo.db/todos dissoc id)))


