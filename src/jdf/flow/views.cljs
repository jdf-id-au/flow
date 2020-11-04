(ns jdf.flow.views
  (:require [helix.core :refer [defnc $ <>]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [jdf.flow.model :as model]))

(defnc selector [{:keys [selected set-selected id value]}]
  (d/button {:class ["button" (when selected "is-selected is-success")]
             :onClick #(set-selected id)} (name id) value))

(defnc root []
  (let [[state set-state] (hooks/use-state model/starting-values)
        [selected set-selected] (hooks/use-state :Q)
        {:keys [BSA QI flow flow-adj SVR SVRI HCT DO2]} (model/calcs state)
        {:keys [min max low high typical]} (model/inputs selected)]
    (<> (d/div {:class "buttons has-addons is-centered"}
          (into [] (for [[_ {:keys [id]}] model/inputs]
                     ; Need to pass a literal map as second argument to $ macro.
                     ($ selector {:id id :key id ; :key is for React, not for me!
                                  :selected (= selected id) :set-selected set-selected
                                  :value (id state)}))))
        (d/input {:type "range" :min min :max max :value (selected state)
                  :onChange #(set-state (fn [s]
                                          (assoc s selected (.. % -target -value))))}))))