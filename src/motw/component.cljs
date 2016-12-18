;; This namespace contains all rum components and some
;; internal helper with type model->view-model
(ns motw.component
  (:require [rum.core :as r]
            [clojure.string :as str]
            [motw.controllers :refer [state dispatch]]
            [motw.utils :as u]
            [motw.maps :as m]))

(defn- filter-item
  "Check is item match search query."
  [search {title :title :as z}]
  (str/includes? (str/lower-case title) (str/lower-case search)))

(defn- with-title-key
  [component]
  #(r/with-key (component %) (:title %)))


(defn- split
  "Split items map to checked and unchecked.
  Returns [checked unchecked]"
  [movies]
  (let [group-fn #(get % :checked? false)
        {checked true unchecked false} (group-by group-fn movies)]
    [checked unchecked]))

(defn- title->key
  [title]
  (str/replace title #"\s" ""))

(r/defc +stepper
  "Represent general step component."
  [steps]
  (let [steps' (interpose :delim steps)]
    [:.stepper
     (map-indexed (fn [i {t :title s? :selected? d? :disabled? click :on-click :as step}]
                    (if (= step :delim)
                      [:.stepper--delim {:key i}]
                      [:.stepper--step  {:key i
                                         :on-click #(when-not d? (click))
                                         :class [(when s? "stepper--step_selected")
                                                 (when d? "stepper--step_disabled")]}
                       t]))
                  steps')]))

(r/defc +steps
  "App step component."
  [current]
  (let [cnt #(count (filter :checked? (vals %)))
        lc (cnt (:locations @state))
        mc (cnt (:movies @state))]
    (+stepper
      [{:title "Movies" :selected? (= current :movies)
        :on-click #(dispatch :change-page :movies)}
       {:title "Locations" :selected? (= current :locations)
        :disabled? (= mc 0) :on-click #(dispatch :change-page :locations)}
       {:title "Route" :selected? (= current :results)
        :disabled? (= lc 0) :on-click #(dispatch :change-page :results)}])))

(r/defc +search
  [{v :value p :placeholder} cb]
  [:input.search {:on-key-up (fn [e] (cb (.. e -target -value)))
                  :type "text"
                  :placeholder (or p "Filter")
                  :value v
                  :on-change (fn [e] (cb (.. e -target -value)))}])

(r/defc +item-header
  [title {:keys [opened? checked?]} {:keys [on-toggle on-open]}]
  [:.item-header
   [:span.item-header--open   {:class (when opened? "item-header--open_yes")
                               :on-click #(on-open)} "⌵"]
   [:h3.item-header--title    {:on-click #(on-open)} title]
   [:span.item-header--toggle {:on-click #(on-toggle)} (if checked? "×" "+")]])

(r/defc +movie-prop
  [value title]
  [[:.movie--label title]
   [:.movie--value value]])

(r/defc +movie
  [{:keys [title opened? checked? year director actors genre plot imdb] :as m}]
  [:.movie {:class (when opened? "movie_opened")}
   (+item-header title m {:on-toggle #(dispatch :toggle-movie {:title title})
                          :on-open   #(dispatch :open-movie {:title title})})
   (when opened?
     [:.movie--content
      (->> [director "Director" year "Year" genre "Genre"
            actors   "Actors"   imdb "IMDB" plot "Plot"]
           (partition 2)
           (filter first)
           (map (fn [[v l]] [:.movie--prop {:key l}
                             [:.movie--label l] [:.movie--value v]])))])])

(r/defc +movies-page
  [{{s :movies} :search m :movies}]
  (let [movies (vals m)
        [checked unchecked] (split movies)
        sorter (partial sort-by :title)
        search-filter (partial filter-item (or s ""))
        unchecked' (filter search-filter unchecked)
        +movie' (with-title-key +movie)
        checked-count (count checked) ]
    [:.movies-page
     [:header.header
      (+steps :movies)
      [:.header--text
       "Select movies you interested in. You need at least one movie."]]
     [:.movies-page--content

      [:.movies-page--unchecked
       (+search s #(dispatch :search {:type :movies :value %}))
       (map +movie' (sorter unchecked'))]
      [:.movies-page--checked
       (map +movie' (sorter checked))
       (when (= checked-count 0)
         [:.movies--empty "Chose at least one movie to continue"])
       [:.movies--action
        [:button
         {:disabled (= checked-count 0)
          :on-click #(dispatch :change-page :locations)}
         "Select locations"]]]]]))

(r/defc +location
  [{:keys [title opened? checked? facts lng lat] :as m}]
  [:.location {:class (when opened? "location_opened")}
   (+item-header title m {:on-toggle #(dispatch :toggle-location {:title title})
                          :on-open   #(dispatch :open-location {:title title})})
   (when opened?
     [:.location--content
      [:.location--facts facts]
      [:.location--image
       {:style {:background-image (str "url(" (m/point-image (:maps-key @state)
                                                             {:lat lat
                                                              :lng lng}) ")")}}]])])

(r/defc +locations-page
  [{ms :movies ls :locations}]
  (let [available (u/checked-movies-locations ms ls)
        [checked unchecked] (split available)
        can-build? (> (count checked) 0)
        steps [{:title "Movies" :on-click #(dispatch :change-page :movies)}
               {:title "Locations" :selected? true} {:title "Route"}]]
    [:.locations-page
     [:header.header
      (+steps :locations)
      [:.header--text
       "Select up to "
       [:span.locations-page--number 8]
       " locations you want to visit."]]
     [:.locations-page--content
      [:.locations-page--unchecked
       (map (with-title-key +location) unchecked)]
      [:.locations-page--checked
       (map (with-title-key +location) checked)
       (when-not can-build?
        [:.locations-page--empty "Chose at least one locations to continue"])
       [:.locations-page--action
        [:button {:disabled (not can-build?)
                  :on-click #(dispatch :change-page :results)}
         "Build route"]]]]]))

(r/defcs +route < {:did-mount
                   #(m/render-route (r/dom-node %) (first (:rum/args %)))}
  "Component for rendering GMAP route."
  [_ route]
  [:.route])

(r/defc +print-list
  [{ms :movies ls :locations}]
  (let [available (u/checked-movies-locations ms ls)
        [checked] (split available)]
    [:.print-list
     (map (fn [{t :title f :facts}] [:.print-list--item [:b t] [:br] f])
          checked)]))

(r/defc +results-page
  [{ms :movies ls :locations {:keys [loading? ok? route]} :route :as s}]
  [:.results-page
   [:header.header
    (+steps :results)
    [:.header--text
     (cond
       loading? "Give us a minute. We prepare your route."
       ok? "You route is ready. Select available options."
       :default [:.results-page--cant
                 "Sorry, but we can not build route for these locations."
                 [:br][:br]
                 [:button {:on-click #(dispatch :change-page :locations)}
                  "Select different locations"]])]]
   (when ok?
    [:.results-page--content
     [:.results-page--route
      (+route route)
      (+print-list s)]
     [:.results-page--variants
      [:.results-page--variant
       "Print the map and enjoy your free trip. Thank you."
       [:br][:br]
       [:button {:on-click #(dispatch :print)} "Print map"]]
      [:.results-page--variant
       "Rent our bike. Each bike has GPS navigation system and voice guide about all places you interested in."
       [:br][:br]
       [:button {:disabled true} "Rent a bike"]]
      [:.results-page--variant
       "Order a tour with our professional guide."
       [:br][:br]
       [:button {:disabled true} "Request a call"]]]])])

(r/defc +app
  "Root app component. Provide simple routing."
  [s]
  [:.page
   ((case (:page s)
      :locations +locations-page
      :results +results-page
      +movies-page)
    s)])
