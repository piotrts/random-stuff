(ns notodo.tests
  (:require [clojure.test :as t]
            [clojure.java.io :as io])
  (:import (org.openqa.selenium WebDriver By TimeoutException)
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

(def ^:dynamic *chrome-web-driver* nil)

(defn setup-chrome-web-driver [f]
  (let [chrome-service (make-and-start-chrome-service)]
    (binding [*chrome-web-driver* (make-chrome-web-driver chrome-service)]
      (.get *chrome-web-driver* "http://localhost:3449")
      (try (. (WebDriverWait. *chrome-web-driver* 10)
              (until (reify ExpectedCondition
                       (apply [_ driver]
                         (seq (.findElements driver (By/cssSelector "#app > *")))))))
           (f)
           (catch TimeoutException e
             (throw (ex-info "Unable to mount app. Is FigWheel running?" {}))))
      *chrome-web-driver*)))

(defn teardown-chrome-web-driver [chrome-web-driver]
  (.quit chrome-web-driver))

(t/use-fixtures :once #(-> % setup-chrome-web-driver teardown-chrome-web-driver))

;; example test
(t/deftest test-elements-after-startup
  (t/testing "presence of at least one todo item"
    (t/is (.findElement *chrome-web-driver* (By/className "todo-item")))))

(defn run-tests []
  (t/run-tests 'notodo.tests))
