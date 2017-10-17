(ns scraps.redis-pubsub
  (:require [clojure.core.async :as a]
            [taoensso.carmine :as car]
            [kdtree]))

(def server-conn
  {:pool {}
   :spec {:uri "redis://127.0.0.1:6379"}})

(defmacro with-conn [& body]
  `(car/wcar server-conn ~@body))

(defn test-redis-connection []
  (assert (= "PONG" (with-conn (car/ping)))))

(comment
  (def triggers
    [{:person/id    10
      :notify/type  :above
      :notify/price 1000}
     {:person/id    20
      :notify/type  :above
      :notify/price 1005}
     {:person/id    10
      :notify/type  :above
      :notify/price 1100}
     {:person/id    20
      :notify/type  :above
      :notify/price 900}
     {:person/id    30
      :notify/type  :below
      :notify/price 990}]))

;; random triggers
(def triggers
  (repeatedly 1000 (fn []
                     {:person/id    (rand-int 10)
                      :notify/type  (rand-nth [:above :below])
                      :notify/price (rand-int 2000)})))

(def above-triggers-tree
  (kdtree/build-tree
    (keep (fn [trigger]
            (let [{:keys [notify/type notify/price]} trigger]
              (when (= :above type)
                (with-meta [price] {:trigger trigger}))))
          triggers)))

(def below-triggers-tree
  (kdtree/build-tree
    (keep (fn [trigger]
            (let [{:keys [notify/type notify/price]} trigger]
              (when (= :below type)
                (with-meta [price] {:trigger trigger}))))
          triggers)))

(defn notify-users* [type last-price new-price]
  (let [tree   (if (= type :above)
                 above-triggers-tree
                 below-triggers-tree)
        result (kdtree/interval-search tree [[last-price new-price]])
        ids    (->> result
                 (map #(-> % meta :trigger :person/id))
                 (into #{}))]))
    ;(when (seq ids)
    ;  (println "notifying users" ids)))

(defn notify-users [last-price new-price]
  (let [type (cond
               (< last-price new-price) :above
               (> last-price new-price) :below)]
    (notify-users* type last-price new-price)))

(def listener
  (let [last-price (atom 1000)]
    (car/with-new-pubsub-listener server-conn
      {"price-update" (fn [[t _ new-price]]
                        (when (= "message" t)
                          (let [new-price (Double/parseDouble new-price)]
                            (notify-users @last-price new-price)
                            (reset! last-price new-price))))}
      (car/subscribe "price-update"))))

(defn rand* [n]
  (- (rand n) (/ n 2)))

(defn price-generator [initial-price lower-bound]
  (let [next-price (max lower-bound (+ initial-price (rand* 10)))]
    (lazy-seq
      (cons initial-price (price-generator next-price lower-bound)))))

(def price-publisher-thread
  (Thread. (fn []
             (time
               (loop [[price & rest] (price-generator 1000 0.01)]
                 (with-conn
                   (car/publish "price-update" price))
                 (Thread/sleep 1000)
                 (recur rest)))
             (println "done"))))

(.start price-publisher-thread)

;(.stop price-publisher-thread)
