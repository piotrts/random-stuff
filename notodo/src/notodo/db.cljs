(ns notodo.db
  (:require [cljs.spec.alpha :as s]))

(def default-db
  #::{:todos (sorted-map-by <)
      :next-id 0})
