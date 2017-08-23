(ns notodo.ui
  (:require [notodo.db :as db]
            [notodo.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.dom :as gdom]))

(def placeholder "(empty)")

(defn todo-add-button []
  [:a {:on-click #(rf/dispatch [::events/add-todo ""])
       :href "#"}
   "Add"])

(defn todo-delete-button [id]
  [:div {:class "todo-item-delete-button"
         :on-click #(rf/dispatch [::events/delete-todo id])}
   \u2716])

(defn todo-item-edited [{:keys [::db/id ::db/content]}]
  [:input {:class "todo-item-content-edited"
           :value content
           :placeholder placeholder
           :on-change #(let [val (-> % .-target .-value)]
                         (rf/dispatch [::events/set-property id ::db/content val]))}])

(defn todo-item-not-edited [{:keys [::db/id ::db/content]}]
  [:div {:class "todo-item-content-not-edited"
         :on-click (fn [_]
                     (rf/dispatch [::events/set-edited id]))}
   (or (seq content) placeholder)])

(defn todo-item [{:keys [::db/id] :as todo}]
  (let [edited? (rf/subscribe [::events/edited? id])]
    [:div {:key (str "todo-" id)
           :class "todo-item"}
     (if @edited?
       [todo-item-edited todo]
       [todo-item-not-edited todo])
     [:div {:class "todo-item-sidebar"}
      [todo-delete-button id]]]))

(defn todo-list []
  (let [todos (rf/subscribe [::events/get-todos])]
    (into [:div] (mapv todo-item @todos))))

(defn ui []
  [:div
   [:div.todo-panel
    [todo-add-button]]
   [todo-list]])

(defonce on-click-event-listener
  (.addEventListener js/document
                     "click"
                     (fn [evt]
                       (when-not (gdom/getAncestorByClass (.-target evt) "todo-item" 3)
                         (rf/dispatch [::events/set-edited nil])))))
