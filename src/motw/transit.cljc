;; Library to deal with edn data structures
;; the same way on backend and frontend
(ns motw.transit
  (:require [cognitect.transit :as transit])
  (:import #?(:clj [java.io ByteArrayOutputStream
                    ByteArrayInputStream]))
  (:refer-clojure :exclude [read]))

(def write-handlers
  {})

(def read-handlers
  {})

#?(:cljs
   (defn write [data]
     (transit/write (transit/writer :json {:handlers write-handlers}) data))

   :clj
   (defn write [data]
     (let [out (ByteArrayOutputStream.)
           w   (transit/writer out :json {:handlers write-handlers})
           _   (transit/write w data)
           res (.toString out)]
       (.reset out)
       res)))

#?(:cljs
   (defn read [input]
     (transit/read (transit/reader :json {:handlers read-handlers}) input))

   :clj
   (defn read [input]
     (let [in (ByteArrayInputStream. (.getBytes input))
           r (transit/reader in :json {:handlers read-handlers})]
       (transit/read r))))

