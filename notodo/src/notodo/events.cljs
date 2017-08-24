(ns notodo.events
  (:require [notodo.db :as db]
            [notodo.schema :as schema]
            [cljs.reader :as reader]
            [re-frame.core :as rf]))

(defn save-in-localstorage [db]
  (.setItem js/localStorage "notodo" (pr-str (dissoc db ::db/edit))))

(def save-in-localstorage-interceptor (rf/after save-in-localstorage))

(rf/reg-cofx ::localstorage-data
  (fn [cofx _]
    (let [data (-> js/localStorage
                   (.getItem "notodo")
                   reader/read-string
                   (update ::db/todos #(into db/EMPTY-TODOS %)))]
      (assoc cofx ::localstorage-data data))))

(def check-specs-interceptor (schema/check-specs-interceptor ::db/db))

(def interceptors [check-specs-interceptor
                   save-in-localstorage-interceptor])

(rf/reg-event-fx ::initialise-db [(rf/inject-cofx ::localstorage-data)
                                  check-specs-interceptor]
  (fn [cofx _]
    (let [data (::localstorage-data cofx)]
      (if (seq (::db/todos data))
        {:db data}
        {:dispatch [::add-todo ""]
         :db db/default-db}))))

(rf/reg-sub ::get-todos
  (fn [db _]
    (vals (::db/todos db))))

(rf/reg-event-db ::add-todo interceptors
  (fn [db [_ content]]
    (let [id (::db/next-id db)
          todo {::db/id id
                ::db/created-at (js/Date.)
                ::db/content content}]
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

(rf/reg-sub ::edit?
  (fn [db [_ id]]
    (= id (::db/edit db))))

(rf/reg-event-db ::set-edit interceptors
  (fn [db [_ id]]
    (assoc db ::db/edit id)))

(rf/reg-event-db ::delete-todo interceptors
  (fn [db [_ id]]
    (update db ::db/todos dissoc id)))
