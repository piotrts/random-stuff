(ns notodo.events
  (:require [notodo.db :as db]
            [notodo.schema :as schema]
            [re-frame.core :as rf]))

(def interceptors [schema/check-specs])

(rf/reg-event-db ::initialise-db interceptors
  (fn [_ _]
    db/default-db))

(rf/reg-sub ::get-todos
  (fn [db _]
    (vals (::db/todos db))))

(rf/reg-sub ::get-todo
  (fn [db id]
    (-> db ::db/todos id)))

(rf/reg-event-db ::add-todo interceptors
  (fn [db [_ content opts]]
    (let [id (::db/next-id db)
          editing? (::db/editing? opts)
          todo #::db{:id id
                     :created-at (js/Date.)
                     :content content
                     :editing? editing?}]
      (-> db
        (update ::db/todos assoc id todo)
        (update ::db/next-id inc)))))

(rf/reg-sub ::get-property
  (fn [db [id key]]
    (get-in db [::db/todos id key])))

(rf/reg-event-db ::set-property interceptors
  (fn [db [_ id key val]]
    (assoc-in db [::db/todos id key] val)))

(rf/reg-event-db ::set-property-all interceptors
  (fn [db [_ key val]]
    (let [old-todos (::db/todos db)
          new-todos (into (empty old-todos)
                          (mapv (fn [[idx m]]
                                  [idx (assoc m key val)])
                                old-todos))]
      (assoc db ::db/todos new-todos))))

(rf/reg-event-db ::delete-todo interceptors
  (fn [db [_ id]]
    (update db ::db/todos dissoc id)))
