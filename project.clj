(defproject motw "0.1.0-SNAPSHOT"
  :description "Change me"
  :dependencies [[org.clojure/tools.namespace "0.3.0-alpha2"
                  :exclusions [org.clojure/tools.reader]]
                 [org.clojure/core.async  "0.2.395"]
                 [javax.servlet/servlet-api "2.5"]
                 [cheshire "5.6.3"]
                 [http-kit "2.2.0"]
                 [compojure "1.5.1"]
                 [org.clojure/clojure "1.9.0-alpha12"]
                 [rum "0.10.7" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [rum-mdl  "0.2.0"]
                 [cljsjs/react-dom "15.3.1-0" :exclusions [cljsjs/react]]
                 [cljsjs/react-dom-server "15.3.1-0" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons "15.3.1-0"]
                 [cljsjs/google-maps "3.18-1"]
                 [com.cognitect/transit-cljs  "0.8.239"]
                 [com.cognitect/transit-clj  "0.8.295"]
                 [org.clojure/clojurescript "1.9.229"]
                 [com.cemerick/url  "0.1.1"]
[clj-webdriver  "0.7.2"]
                   [org.seleniumhq.selenium/selenium-server  "3.0.1"]
                 [hiccup "1.0.5"]]
  :main motw.core
  :source-paths ["src"]
  :resource-paths ["resources"]
  :plugins [[lein-figwheel "0.5.7"]]
  :figwheel {:css-dirs ["resources/public/css"]}
  :profiles
  {:dev
   {:main user
    :plugins [[lein-cljsbuild  "1.1.4"]
              [lein-figwheel "0.5.7"]]
    :cljsbuild {:builds [{:id "front"
                         :source-paths ["src/"]
                         :figwheel {:on-jsload "motw.client/reload-hook"}
                         :compiler {:main "motw.client"
                                    :optimizations :none
                                    :asset-path "/public/js/out"
                                    :output-to "resources/public/js/motw.js"
                                    :output-dir "resources/public/js/out"}}]}}
   :prod {:main motw.core}
   :ui
   {:plugins [[lein-cljsbuild  "1.1.4"]
              [lein-figwheel "0.5.7"]]
    :cljsbuild {:builds [{:id "prod"
                          :source-paths ["src/"]
                          :compiler {:main "motw.client"
                                     :optimizations :advanced
                                     :elide-asserts false
                                     :pretty-print false
                                     :asset-path "/public/js/out"
                                     :output-to "resources/public/js/motw.js"
                                     :output-dir "resources/public/js/out"
                                     :source-map "resources/public/js/motw.js.map"}}]}}})
