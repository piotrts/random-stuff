(defproject notodo "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.7.1"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript "1.9.908"]
                 [reagent "0.8.0-alpha1"]
                 [re-frame "0.10.1"]
                 [org.seleniumhq.selenium/selenium-java "3.5.2"]]
  :plugins [[lein-figwheel "0.5.13"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :source-paths ["src"]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "test/src"]
                :compiler {:main notodo.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/notodo.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}
                :figwheel {}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/notodo.js"
                           :main notodo.core
                           :optimizations :advanced
                           :pretty-print false}}]}
  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :source-paths ["src" "dev"]
                   :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})

