(ns jdf.flow.views
  (:require [helix.core :refer [defnc $ <>]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [jdf.flow.model :as model]
            [goog.string :as gstr]))

(defn fix [v precision]
  (try (.toFixed v ({1 0, 0.1 1, 0.01 2} precision))
       (catch :default e (js/console.log v (string?  v)))))

(defnc value [{:keys [id calcs element]}]
  (let [{:keys [precision unit label min max low high]} (model/parameters id)]
    ($ (name element) {:class "value"} (fix (id calcs) precision))))

(defnc single [{:keys [id calcs]}]
  (let [{:keys [precision unit label]} (model/outputs id)]
    (d/div {:class "tile single"}
      (d/div {:class "header"}
        (d/span {:class "id"} (name id))
        (d/span {:class "unit"} unit))
      ($ value {:id id :calcs calcs :element :div})
      (d/div {:class "label"} label))))

(defnc multi [{:keys [ids calcs]}]
  (d/div {:class "tile multi"}
    (into []
      (for [id ids :let [{:keys [unit label]} (model/outputs id)]]
        (d/div {:class "row" :key id}
          ; use hier css selector to lay out same classes differently
          (d/span {:class "id"} (name id))
          ($ value {:id id :calcs calcs :element :span})
          (d/span {:class "unit"} unit))))))
          ; TODO where to put label?

(defnc button [{:keys [selected set-selected id value]}]
  (let [{:keys [precision]} (model/inputs id)]
    (d/button {:class [(when selected "selected")]
               :onClick #(set-selected id)}
      (d/span {:class "name"} (name id))
      (d/span {:class "value"} (fix value precision)))))

(defnc root []
  (let [[state set-state] (hooks/use-state model/starting-values)
        [selected set-selected] (hooks/use-state :Q)
        calcs (model/calcs state)
        {:keys [label min max precision unit low high typical]} (model/inputs selected)]
    (<>
      (d/header
        (d/h1 "Perfusion toy")
        (d/h2 "Not for clinical use!"))
      (d/main
        ($ single {:id :QI :calcs calcs})
        ($ single {:id :DO2 :calcs calcs})
        ($ multi {:ids [:flow :flow-adj] :calcs calcs})
        ($ multi {:ids [:SVR :SVRI] :calcs calcs})
        ($ multi {:ids [:BMI :BSA] :calcs calcs}))
      (d/footer
        (d/div {:class "button-bar"}
          (into [] (for [id model/input-order]
                     ; Need to pass a literal map as second argument to $ macro.
                     ($ button {:id id :key id ; :key is for React, not me!
                                :value (id state)
                                :selected (= selected id)
                                :set-selected set-selected}))))
        (d/div {:class "slider-widget"}
          (d/div {:class "label"} label)
          (d/input {:type "range" :min min :max max :step precision
                    :class "range" ; better css selector?
                    :value (selected state)
                    :onChange #(set-state ; range input returns string ugh
                                 (fn [s] (assoc s selected
                                           (js/parseFloat (.. % -target -value)))))})
          (d/div {:class "scale"}
            (d/span min)
            (d/span (fix (selected state) precision) \space unit)
            (d/span max)))))))