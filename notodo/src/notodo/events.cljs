(ns notodo.events
  (:require [notodo.db :as db]
            [notodo.schema :as schema]
            [re-frame.core :as rf]))

(def interceptors [(schema/check-specs ::db/db)])

(rf/reg-event-fx ::initialise-db interceptors
  (fn [cofx _]
    {:dispatch [::add-todo ""]
     :db db/default-db}))

(rf/reg-sub ::get-todos
  (fn [db _]
    (vals (::db/todos db))))

(rf/reg-event-db ::add-todo interceptors
  (fn [db [_ content]]
    (let [id (::db/next-id db)
          todo #::db{:id id
                     :created-at (js/Date.)
                     :content content}]
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

(rf/reg-sub ::edited?
  (fn [db [_ id]]
    (= id (::db/edited db))))

(rf/reg-event-db ::set-edited interceptors
  (fn [db [_ id]]
    (assoc db ::db/edited id)))

(rf/reg-event-db ::delete-todo interceptors
  (fn [db [_ id]]
    (update db ::db/todos dissoc id)))
