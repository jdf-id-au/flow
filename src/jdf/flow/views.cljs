(ns jdf.flow.views
  (:require [helix.core :refer [defnc $ <>]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [jdf.flow.model :as model]
            [goog.string :as gstr]))

(def precision-map "ugh"
  {1 0
   0.1 1
   0.01 2})

(defnc output [{:keys [id calcs]}]
  (let [{:keys [precision unit]} (model/outputs id)]
    (d/p
      (d/b (name id))
      (.toFixed (id calcs) (precision-map precision))
      unit)))

(defnc input [{:keys [selected set-selected id value]}]
  (d/button {:class ["button" (when selected "is-selected is-success")]
             :onClick #(set-selected id)} (d/b (name id)) value))

(defnc root []
  (let [[state set-state] (hooks/use-state model/starting-values)
        [selected set-selected] (hooks/use-state :Q)
        calcs (model/calcs state)
        {:keys [label min max precision unit low high typical]} (model/inputs selected)]
    (<>
      (d/h1 "Perfusion toy")
      (d/div
        (into [] (for [id model/output-order :when (id calcs)]
                   ($ output {:id id :key id :calcs calcs}))))
      (d/div {:class "buttons has-addons is-centered"}
        (into [] (for [id model/input-order]
                   ; Need to pass a literal map as second argument to $ macro.
                   ($ input {:id id :key id ; :key is for React, not for me!
                             :selected (= selected id) :set-selected set-selected
                             :value (id state)}))))
      (d/div (d/strong label)
        min
        (d/input {:type "range" :min min :max max :step precision
                  :value (selected state)
                  :onChange #(set-state (fn [s]
                                          (assoc s selected (.. % -target -value))))})
        max
        unit)
      (d/small "This a toy. Not for clinical use!"))))