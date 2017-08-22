(ns notodo.core
    (:require [reagent.core :as r]
              [re-frame.core :as rf]))

(enable-console-print!)

(rf/reg-event-db :initialise-db
  (fn [_ _]
    {:todos (sorted-map-by <)
     :next-id 0}))

(rf/reg-sub :query-todos
  (fn [db _]
    (vals (:todos db))))

(rf/reg-event-fx :add-todo
  (fn [{:keys [db]} content]
    (let [id (:next-id db)
          todo {:id id
                :created-at (js/Date.)
                :content content}]
      {:db (-> db
             (update :todos assoc id todo)
             (update :next-id inc))})))

(rf/reg-event-fx :delete-todo
  (fn [{:keys [db]} id]
    {:db (update db :todos dissoc id)}))

(defn todo-add-button []
  [:button {:on-click #(rf/dispatch [:add-todo "val"])}
   "Add"])

(defn todo-delete-button [id]
  [:button {:on-click #(rf/dispatch [:delete-todo id])}
   "Delete"])

(defn todo-item [{:keys [id content] :as todo}]
  [:div {:key id}
   content
   [todo-delete-button id]])

(defn todo-list []
  (let [todos (rf/subscribe [:query-todos])]
    [:div (map todo-item @todos)]))

(defn ui []
  [:div
   [todo-add-button]
   [todo-list]])

(rf/dispatch [:initialise-db])
(r/render [ui] (. js/document (getElementById "app")))

