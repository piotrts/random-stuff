(ns notodo.tests
  (:require [clojure.java.io :as io])
  (:import (org.openqa.selenium.chrome ChromeDriverService ChromeDriverService$Builder)
           (org.openqa.selenium.remote DesiredCapabilities RemoteWebDriver)))

(def chrome-executable-path "/opt/chromedriver/chromedriver") ;; yeah, its hardcoded

(defn make-and-start-chrome-service []
  (let [service (.. (ChromeDriverService$Builder.)
                    (usingDriverExecutable (io/file chrome-executable-path))
                    (usingAnyFreePort)
                    (build))]
    (.start service)
    service))

(defn make-chrome-web-driver [service]
  (RemoteWebDriver. (.getUrl service)
                    (DesiredCapabilities/chrome)))

(def chrome-service (atom nil))
(def chrome-web-driver (atom nil))

(defn run-web-tests []
  (reset! chrome-service (make-and-start-chrome-service))
  (reset! chrome-web-driver (make-chrome-web-driver @chrome-service))
  (.get @chrome-web-driver "http://localhost:3449"))

;(run-web-tests)

