(ns notodo.core
  (:require [notodo.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.dom :as gdom]))

(enable-console-print!)

(defn todo-add-button []
  [:button {:on-click #(rf/dispatch [::events/add-todo "val"])}
   "Add"])

(defn todo-delete-button [id]
  [:button {:on-click #(rf/dispatch [::events/delete-todo id])}
   "Delete"])

(defn todo-item [{:keys [:notodo.db/id :notodo.db/content :notodo.db/editing?]}]
  [:div {:key id
         :class "todo-item"}
   (if editing?
     [:input {:class "todo-item-content"
              :value content
              :on-change #(let [val (-> % .-target .-value)]
                            (rf/dispatch [::events/set-property id :notodo.db/content val]))}]
     [:div {:class "todo-item-content"
            :on-click (fn [_]
                        (rf/dispatch [::events/set-property-all :notodo.db/editing? false])
                        (rf/dispatch [::events/set-property id :notodo.db/editing? true]))}
      content])
   [:div {:class "todo-item-sidebar"}
    [todo-delete-button id]]])

(defn todo-list []
  (let [todos (rf/subscribe [::events/get-todos])]
    [:div (map todo-item @todos)]))

(defn ui []
  [:div
   [todo-add-button]
   [todo-list]])

(rf/dispatch [::events/initialise-db])

(defonce on-click-event-listener
  (.addEventListener js/document
                     "click"
                     (fn [evt]
                       (when-not (gdom/getAncestorByClass (.-target evt) "todo-item" 3)
                         (rf/dispatch [::events/set-property-all :notodo.db/editing? false])))))

(r/render [ui] (. js/document (getElementById "app")))

