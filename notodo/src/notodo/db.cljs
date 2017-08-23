(ns notodo.db)

(def EMPTY-TODOS (sorted-map-by >))

(def default-db
  #::{:todos EMPTY-TODOS 
      :next-id 0})
