(ns notodo.db)

(def default-db
  #::{:todos (sorted-map-by <)
      :next-id 0})
