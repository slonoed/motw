(ns motw.selenium-test
  (:require [clj-webdriver.taxi :as t]
            [clojure.core.async :as a]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(def url "http://localhost:8080")
(def route-url (str url "/route"))

(declare timeout)

;; Setup and teardown browser with each test
(defn selenium-fixture
  [& browsers]
  (fn [test]
    (doseq [browser browsers]
      (println (str "\n[ Testing " browser " ]"))
      (t/set-driver! {:browser browser})
      (test)
      (t/quit))))

(use-fixtures :once  (selenium-fixture :firefox))

(deftest landing
  (t/to url)
  (t/click ".landing--action")
  (t/wait-until #(t/exists?  ".search"))
  (is (= route-url (t/current-url))))

(deftest search
  (t/to route-url)
  (t/input-text ".search" "Chan")
  (is (> 20 (count (t/elements ".item-header--title")))))

(deftest open-movie
  (t/to route-url)
  (t/click ".item-header--open")
  (is (= (t/text ".movie--value") "Jayendra")))

(deftest toggle-few
  (t/to route-url)
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (is (= 3 (count (t/elements ".movies-page--checked .movie")))))

(deftest select-locations
  (t/to route-url)
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".movies--action button")
  (t/wait-until (timeout 100))
  (is (= 9 (count (t/elements ".locations-page--unchecked .location")))))

(deftest built-route
  (t/to route-url)
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".movies--action button")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".item-header--toggle")
  (t/wait-until (timeout 100))
  (t/click ".locations-page--action button")
  (t/wait-until #(str/includes? (t/text ".header--text") "You route is ready"))
  (is (t/exists? ".gm-style")))


(defn- timeout
  "Return function that return false until ms timeout expired."
  [ms]
  (let [released? (atom false)]
    (a/go (a/<! (a/timeout ms))
          (reset! released? true))
    (fn [] @released?)))
