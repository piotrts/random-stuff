(ns notodo.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.dom :as gdom]))

(enable-console-print!)

(rf/reg-event-db ::initialise-db
  (fn [_ _]
    {:todos (sorted-map-by >)
     :next-id 0}))

(rf/reg-sub ::get-todos
  (fn [db _]
    (vals (:todos db))))

(rf/reg-sub ::get-todo
  (fn [db id]
    (-> db :todos id)))

(rf/reg-event-fx ::add-todo
  (fn [{:keys [db]} [_ content opts]]
    (let [id (:next-id db)
          editing? (:editing? opts)
          todo {:id id
                :created-at (js/Date.)
                :content content
                :editing? editing?}]
      {:db (-> db
             (update :todos assoc id todo)
             (update :next-id inc))})))

(rf/reg-sub ::get-property
  (fn [db [id key]]
    (get-in db [:todos id key])))

(rf/reg-event-fx ::set-property
  (fn [{:keys [db]} [_ id key val]]
    {:db (assoc-in db [:todos id key] val)}))

(rf/reg-event-fx ::set-property-all
  (fn [{:keys [db]} [_ key val]]
    (let [old-todos (:todos db)
          new-todos (into (empty old-todos)
                          (mapv (fn [[idx m]]
                                  [idx (assoc m key val)])
                                old-todos))]
      {:db (assoc db :todos new-todos)})))

(rf/reg-event-fx ::delete-todo
  (fn [{:keys [db]} [_ id]]
    {:db (update db :todos dissoc id)}))

(defn todo-add-button []
  [:button {:on-click #(rf/dispatch [::add-todo "val"])}
   "Add"])

(defn todo-delete-button [id]
  [:button {:on-click #(rf/dispatch [::delete-todo id])}
   "Delete"])

(defn todo-item [{:keys [id content editing?]}]
  [:div {:key id
         :class "todo-item"}
   (if editing?
     [:input {:value content
              :on-change #(let [val (-> % .-target .-value)]
                            (rf/dispatch [::set-property id :content val]))}]
     [:div {:on-click (fn [_]
                        (rf/dispatch [::set-property-all :editing? false])
                        (rf/dispatch [::set-property id :editing? true]))}
      content])
   [todo-delete-button id]])

(defn todo-list []
  (let [todos (rf/subscribe [::get-todos])]
    [:div (map todo-item @todos)]))

(defn ui []
  [:div
   [todo-add-button]
   [todo-list]])

(rf/dispatch [::initialise-db])

(defonce on-click-event-listener
  (.addEventListener js/document
                     "click"
                     (fn [evt]
                       (when-not (gdom/getAncestorByClass (.-target evt) "todo-item" 3)
                         (rf/dispatch [::set-property-all :editing? false])))))

(r/render [ui] (. js/document (getElementById "app")))

