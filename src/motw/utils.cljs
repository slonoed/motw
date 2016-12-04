(ns motw.utils)

(defn only-checked
  "Return vec of checked moves."
  [movies]
  (->> movies
       vec
       (map second)
       (filter :checked?)))

(defn checked-movies-locations
  "Return locations vec from checked movies."
  [movies locations]
  (->> movies
       only-checked
       (map :locations)
       (apply concat)
       set vec ;; remove duplicates
       (map #(get locations %))
       (remove nil?)))
