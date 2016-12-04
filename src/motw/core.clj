(ns motw.core
  (:require [hiccup.core :as hiccup]
            [motw.transit :as transit]
            [cheshire.core :as json])
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server))

(def GMAPS_KEY (System/getenv "GMAPS_KEY"))
(def GTM_KEY (System/getenv "GTM_KEY"))


;; Google tag manager
(def gtm-script-tag
  [:script (str "(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
								 new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
								 j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
								 'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
								 })(window,document,'script','dataLayer','"
								GTM_KEY
								"');")])

(defn- render
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (hiccup/html
              "<!doctype html>"
              [:html
               [:head
                [:base {:href "/route"}]
                [:meta {:charset "utf-8"}]
                [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
                [:link {:rel "shortcut icon" :type "image/x-icon" :href "/public/bicycle.png"}]
                [:link {:rel "stylesheet" :media "screen,projection,print"
                        :href "https://fonts.googleapis.com/css?family=Open+Sans:100,300,400"}]
                [:link {:rel "stylesheet" :media "screen,projection,print"
                        :href  "/public/css/motw.css"}]
                gtm-script-tag]
               [:body
                [:div#application]
                [:script#initial-data {:type "application/edn"}
                 (transit/write {:maps-key GMAPS_KEY
                                 :movies (transit/read (slurp "resources/movies-e2.edn"))
                                 :locations (transit/read (slurp "resources/locations.edn"))})]
                [:script {:src (str "https://maps.googleapis.com/maps/api/js?key=" GMAPS_KEY)}]
                [:script {:src "/public/js/motw.js"}]
                [:script
                 "motw.client.init()"]]])})

(defn- landing [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (hiccup/html
     [:html
      [:head
       [:meta {:charset "utf-8"}]
       [:link {:rel "shortcut icon" :type "image/x-icon" :href "/public/bicycle.png"}]
       [:link {:rel "stylesheet" :media "screen"
               :href "https://fonts.googleapis.com/css?family=Open+Sans:100,300,400"}]
       [:link {:rel "stylesheet" :media "screen,projection,print"
               :href  "/public/css/motw.css"}]
       gtm-script-tag]
     [:body.landing
      [:div.landing--header
       "Get new cool experience."
       [:ul.landing--list
        [:li.landing--item "Choose interesting movies"]
        [:li.landing--item "Select locations you want to visit"]
        [:li.landing--item "Enjoy you tour!"]]
       [:a.button.landing--action {:href "/route"} "I want it!"]]]])})


(defroutes all-routes
  (GET "/" [] landing)
  (GET "/route" [] render)
  (files "/public/" {:root "resources/public"})
  (not-found "Page not found"))

(def app (site all-routes))

(defn -main
  [& args]
  (run-server app {:port 8080}))
