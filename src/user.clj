;; user namespace used from REPL sessions in dev
(ns user
  (:require [motw.core :as b]
            [org.httpkit.server :as kit]
            [cheshire.core :as json]
            [clojure.string :as str]
            [motw.transit :as transit]
            [org.httpkit.client :as http]
            [clojure.core.async :as a]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defonce server (atom nil))


(defn go []
  (reset! server (kit/run-server #'b/app {:port 8080})))

(defn reset
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil))
  (refresh :after 'user/go))

;; selenium

(defn run-selenium
  []
  (test/run-tests 'motw.selenium-test))

;; tools

;; I keep this code here only for demonstrating how I playing with data.
;; This is NOT production code. More like sandbox for experiments.

(def gmap-key "PLACE KEY HERE")

(defn conv-item [arr]
  {:id (get arr 1)
   :title (get arr 8)
   :year (get arr 9)
   :location-name (get arr 10)
   :facts (get arr 11)
   :director (get arr 14)})

(def conv-items #(map conv-item %))

;; store data as map
;; key - movie title
;; value - map {:year 19990 :director "" :locations {"location title" {:facts}}}
(defn denorm
  [a {:keys [id title year location-name facts director location]}]
  (-> a
      (assoc-in [title :locations location-name :facts] facts)
      (assoc-in [title :year] year)
      (assoc-in [title :director] director)
      (assoc-in [title :locations location-name :lng] (:lng location))
      (assoc-in [title :locations location-name :lat] (:lat location))))

(defn convert-raw-data-denorm [data]
  (as-> data d
    (json/parse-string d true)
    (reduce denorm {} d)
    (json/generate-string d {:pretty true})))
(defn convert-raw-data-denorm-edn [data]
  (as-> data d
    (json/parse-string d true)
    (reduce denorm {} d)
    (transit/write d)))

(defn convert-raw-data [data]
  (as-> data d
    (json/parse-string d true)
    (:data d)
    (conv-items d)
    (json/generate-string d {:pretty true})))

(defn pipe [in process out]
  (->> (slurp (str "resources/" in))
       process
       (spit (str "resources/" out))))

(defn resolve-name
  "Return coordinates of location by address
  Coords is a map {:lat :lng}.
  Return :not-found if not found (:"
  [address]
  (a/<!! (a/timeout 40))
  (let [{:keys [body]}
        @(http/get "https://maps.googleapis.com/maps/api/geocode/json"
                   {:query-params {:key gmap-key
                                   :address address
                                   :bound "36.896139,-122.978741|38.285361,-121.781141"}})
        {:keys [results status]} (json/parse-string body true)]
    (if (= status "OK")
      (println "Fetched" address)
      (println "Error when fetch for" address " status " status))
    (get-in results [0 :geometry :location] :not-found)))

(defn resolve-names [data]
  (json/generate-string (->> (json/parse-string data true)
                             (map #(assoc % :location (resolve-name (:location-name %))))
                             (remove #(= :not-found (:location %))))
                        {:pretty true}))
;; movies {:locations ["" "" ""] }
(defn denorm-m
  [a {:keys [id title year location-name facts director location]}]
  (let [t (str/trim title)] (-> a
       (update-in [t :locations] (fnil conj []) location-name)
       (assoc-in [t :year] year)
       (assoc-in [t :director] director))))

(defn convert-movies [data]
  (as-> data d
    (json/parse-string d true)
    (reduce denorm-m {} d)
    (transit/write d)))

(defn denorm-l
  [a {:keys [id location-name facts location]}]
  (let [n (str/trim location-name)]
    (-> a
       (update-in [n :facts] #(or % facts))
       (assoc-in [n :lng] (:lng location))
       (assoc-in [n :lat] (:lat location)))))

(defn convert-locations [data]
  (as-> data d
    (json/parse-string d true)
    (reduce denorm-l {} d)
    (transit/write d)))

(defn enrich-movies
  [data]
  (as-> data d
  (transit/read d)
  (vec d)
  (map (fn [[title {year :year director :director}]]
         (let [qt (str/replace title " " "+")
               url (str "http://www.omdbapi.com/?t=" qt "&y=" year "&plot=short&r=json")
               {b :body} @(http/get url)
               res (json/parse-string b true)]
           (println url res)
           [title {:year year
                   :director director
                   :genre (:Genre res)
                   :actors (:Actors res)
                   :imdb (:imdbRating res)
                   :plot (:Plot res)}]))
       d)
  (into {} d)
  (transit/write d)) )

(def old (transit/read (slurp "resources/movies.edn")))

(comment
  ; convert data from site to json array with named fields amps
  (pipe "raw-data.json" convert-raw-data "locations-list-raw.json")
  ; 1586 total raw items
  ; 1085 items with coords
  (pipe "locations-list-raw.json" resolve-names "locations-with-coords.json")
  (pipe "locations-with-coords.json" convert-movies "movies.edn")
  (pipe "locations-with-coords.json" convert-locations "locations.edn")
  (pipe "movies.edn" enrich-movies "movies-e.json")
  (pipe "movies-e.edn" (fn [data] (as-> data d
                                    (transit/read d)
                                    (vec d)
                                    (map (fn [[title d]]
                                           [title
                                            (assoc d :locations (get-in old [title :locations]))]) d)
                                    (into {} d)
                                    (transit/write d))) "movies-e2.json")
   
)
