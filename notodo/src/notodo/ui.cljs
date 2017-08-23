(ns notodo.ui
  (:require [notodo.db :as db]
            [notodo.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.dom :as gdom]))

(defn todo-add-button []
  [:button {:on-click #(rf/dispatch [::events/add-todo "val"])}
   "Add"])

(defn todo-delete-button [id]
  [:button {:on-click #(rf/dispatch [::events/delete-todo id])}
   "Delete"])

(defn todo-item [{:keys [::db/id ::db/content ::db/editing?]}]
  (let [edited? (rf/subscribe [::events/edited? id])]
    [:div {:key id
           :class "todo-item"}
     (if @edited?
       [:input {:class "todo-item-content"
                :value content
                :on-change #(let [val (-> % .-target .-value)]
                              (rf/dispatch [::events/set-property id ::db/content val]))}]
       [:div {:class "todo-item-content"
              :on-click (fn [_]
                          (rf/dispatch [::events/set-edited id]))}
        content])
     [:div {:class "todo-item-sidebar"}
      [todo-delete-button id]]]))

(defn todo-list []
  (let [todos (rf/subscribe [::events/get-todos])]
    (into [:div] (mapv todo-item @todos))))

(defn ui []
  [:div
   [todo-add-button]
   [todo-list]])

(defonce on-click-event-listener
  (.addEventListener js/document
                     "click"
                     (fn [evt]
                       (when-not (gdom/getAncestorByClass (.-target evt) "todo-item" 3)
                         (rf/dispatch [::events/set-edited nil])))))
