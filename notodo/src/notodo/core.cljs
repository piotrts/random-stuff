(ns notodo.core
  (:require [notodo.ui :as ui]
            [reagent.core :as r]))

(enable-console-print!)

(r/render [ui/ui] (. js/document (getElementById "app")))

