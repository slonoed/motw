;; Google Maps API wrapper
(ns motw.maps
  (:require [cljs.core.async :as a])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def DirectionsService (.-DirectionsService (.-maps js/google)))
(def DirectionsRenderer (.-DirectionsRenderer (.-maps js/google)))
(def Map (.-Map (.-maps js/google)))

(defn build-route
  "Return channel. Channel provide [error, route]."
  [origin destination points]
  (let [ch (a/chan 1)
        ds (DirectionsService.)
        waypoints (map (fn [l] {:location l :stopover true}) points)
        opts (clj->js {:destination destination
                       :origin (clj->js origin)
                       :waypoints (clj->js waypoints)
                       :optimizeWaypoints true
                       :travelMode "BICYCLING"})
        cb (fn [r s]
             (a/put! ch [(if (= s "OK") nil (js/Error. s))
                         r]))]
    (.route ds opts cb)
    ch))


(defn render-route
  "Render map with route on node."
  [node route]
  (let [center {:lat 37.792370 :lng  -122.396489}
        m (Map. node (clj->js {:center center :scrollwheel false :zoom 16}))
        dir-display (DirectionsRenderer. #js {"map" m})]
    (.setDirections dir-display route)))

(defn point-image
  "Return image url for given API key and params."
  [api-key {:keys [lat lng]}]
  (str "https://maps.googleapis.com/maps/api/staticmap?"
       "zoom=12"
       "&markers=" lat "," lng
       "&size=630x210"
       "&key=" api-key))
