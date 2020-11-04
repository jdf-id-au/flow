(ns jdf.flow.views
  (:require [helix.core :refer [defnc $ <>]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [jdf.flow.model :as model]
            [goog.string :as gstr]))

(defn fix [v precision]
  (try (.toFixed v ({1 0, 0.1 1, 0.01 2} precision))
       (catch :default e (js/console.log v (string?  v)))))

(defnc output [{:keys [id calcs]}]
  (let [{:keys [precision unit label]} (model/outputs id)]
    (d/div {:class ["tile is-child"]}
      (d/div {:class "spread"}
        (d/span {:class "is-size-3"} (name id))
        (d/span)
        (d/span {:class "is-size-5"} unit))
      (d/div {:class "mega has-text-centered"} (fix (id calcs) precision))
      (d/div {:class "subtitle"} label))))

(defnc pair [{:keys [id1 id2 calcs]}]
  (d/div {:class ["tile is-child"]}))

(defnc input [{:keys [selected set-selected id value]}]
  (let [{:keys [precision]} (model/inputs id)]
    (d/button {:class ["button" (when selected "is-selected is-warning")]
               :onClick #(set-selected id)} (d/span (d/b (name id)) \space (fix value precision)))))

(defnc root []
  (let [[state set-state] (hooks/use-state model/starting-values)
        [selected set-selected] (hooks/use-state :Q)
        calcs (model/calcs state)
        {:keys [label min max precision unit low high typical]} (model/inputs selected)]
    (d/section {:class "hero is-dark is-bold is-fullheight"}
      (d/div {:class "hero-head"}
        (d/div {:class "container"}
          (d/h1 {:class "title"} "Perfusion toy")
          (d/h2 {:class "subtitle"} "Not for clinical use!")))
      (d/div {:class "hero-body"}
        (d/div {:class "container"}
          (d/div {:class "tile is-ancestor"}
            (d/div {:class "tile is-parent is-vertical is-8"}
              ($ output {:id :QI :calcs calcs})
              #_(into [] (for [id model/output-order :when (id calcs)]
                           ($ output {:id id :key id :calcs calcs}))))
            (d/div {:class "tile is-parent"}
              ($ output {:id :DO2 :calcs calcs})))))
      (d/div {:class "hero-foot"}

        (d/div {:class "container"}
          (d/div {:class "buttons has-addons is-centered"}
            (into [] (for [id model/input-order]
                       ; Need to pass a literal map as second argument to $ macro.
                       ($ input {:id id :key id ; :key is for React, not for me!
                                 :selected (= selected id) :set-selected set-selected
                                 :value (id state)}))))
          (d/div {:class "spread"} (d/span) label (d/span))
          (d/input {:type "range" :min min :max max :step precision
                    :style {:width "100%"}
                    :value (selected state)
                    :onChange #(set-state ; range input returns text ugh
                                 (fn [s] (assoc s selected (js/parseFloat (.. % -target -value)))))})
          (d/div {:class "spread"}
            min (d/span) (fix (selected state) precision) \space unit (d/span) max))))))