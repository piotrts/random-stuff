(ns notodo.tests
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :as io])
  (:import (org.openqa.selenium WebDriver By)
           (org.openqa.selenium.chrome ChromeDriverService ChromeDriverService$Builder)
           (org.openqa.selenium.remote DesiredCapabilities RemoteWebDriver)
           (org.openqa.selenium.support.ui ExpectedCondition)
           (org.openqa.selenium.support.ui WebDriverWait)))

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

(defn run-web-tests []
  (let [chrome-service (make-and-start-chrome-service)
        chrome-web-driver (make-chrome-web-driver chrome-service)]
    (.get chrome-web-driver "http://localhost:3449")
    (. (WebDriverWait. chrome-web-driver 10)
       (until (reify ExpectedCondition
                (apply [_ d]
                  (.findElement d (By/id "app"))))))
    (.quit chrome-web-driver)))

;(run-web-tests)

