(ns notodo.core
  (:require [notodo.ui :as ui]
            [notodo.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(enable-console-print!)

(rf/dispatch [::events/initialise-db])

(r/render [ui/ui] (. js/document (getElementById "app")))

