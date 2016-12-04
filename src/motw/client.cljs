;; Frontend entry point.
;; Separate this namespace from others helps with hot reload.
(ns motw.client
  (:require [motw.controllers :as ctrls]
            [motw.component :as c]
            [rum.core :as r]
            [motw.transit :as transit]
            [cljs.reader :as reader]))


(defn load-init-state []
  (transit/read (.-text (js/document.getElementById "initial-data"))))

(r/defc wrapper < r/reactive
  [s]
  (c/+app (r/react s)))

(defn ^:export init
  "Main entry point. Mount app and start app."
  []
  (enable-console-print!)
  (let [state (atom (load-init-state))]
    (ctrls/init-state state)
    (r/mount (wrapper state) (js/document.getElementById "application"))
    (ctrls/start!)))

(defn ^:export reload-hook
  "Used by figwheel to provide hot reload.
  When state changed app rerender."
  [_]
  (swap! ctrls/state update :reload-count (fnil inc 0)))
