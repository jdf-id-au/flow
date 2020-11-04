(ns jdf.flow.model
  "Physiology parameters, units, values, ranges and calculations.")

; FIXME be very careful when suggesting "normal" ranges! *** not fixed yet

(defrecord Parameter [id min max unit label low high typical])
(def field-count (-> {} map->Parameter count))

(def -inputs ; Use decimal/floating-point with all these
  [:Q 0 7 "L/min" "systemic blood flow" nil nil 3
   :MAP 0 300 "mmHg" "mean arterial pressure" 60 100 65
   :CVP -10 100 "mmHg" "central venous pressure" -5 20 5
   :SvO2 0 100 "%" "central venous oxygen saturation" 60 90 75
   :Hb 0 250 "g/L" "haemoglobin concentration" 60 100 80
   :T 15 40 "°C" "temperature" 18 37 34
   :SaO2 0 100 "%" "arterial oxygen saturation" 95 nil 100
   :PaO2 0 800 "mmHg" "arterial oxygen partial pressure" 400 nil 500
   :height 75 250 "cm" "height" 120 200 170
   :weight 40 250 "kg" "weight" 40 120 70])
; sex...

(def -outputs
  ; ugh https://www.lidco.com/education/normal-hemodynamic-parameters/
  [:BSA 0 3 "sq m" "body surface area" nil nil 1.9
   :QI 0 15 "L/min/m²" "systemic blood flow index" 1.8 3 2.4
   :flow 0 150 "%" "percentage of full flow" 80 120 100
   :flow-adj 0 150 "%" "percentage of full flow, temperature-adjusted" 80 120 100
   :SVR 0 3000 "dyn⋅s⋅cm⁻⁵" "systemic vascular resistance" 800 1200 1000
   :SVRI 0 4000 "dyn⋅s⋅cm⁻⁵/m²" "systemic vascular resistance index" 1970 2390 nil
   :HCT 0 80 "%" "haematocrit" 20 45 40
   :CaO2 0 400 "ml/L" "arterial oxygen content" 170 200 180
   ;:CvO2 0 400 "ml/L" "venous oxygen content" 120 150 130
   :DO2 0 2000 "mL/min" "oxygen delivery" 950 1150 1000])

(defn parts [raw]
  (assert (zero? (mod (count raw) field-count)))
  (partition field-count raw))

(defn labelled
  [parts]
  (into {} (for [row parts :let [rec (apply ->Parameter row)]] [(:id rec) rec])))

(def input-order (->> -inputs parts (map first)))
(def inputs (->> -inputs parts labelled))
(def starting-values (into {} (for [[k {v :typical}] inputs] [k v])))
(def output-order (->> -outputs parts (map first)))
(def outputs (->> -outputs parts labelled))

(defn BSA "Body surface area (DuBois)"
  ; https://www.uptodate.com/contents/calculator-body-surface-area-in-adults-du-bois-method
  ; TODO memoize candidate?
  [height weight]
  (* 0.007184
    (Math/pow height 0.725)
    (Math/pow weight 0.425)))

(defn adjusted
  "Adjust for temperature"
  [x T]
  (let [Tbody 37. per-degree -0.07]
    (* x (+ 1.0 (* (- Tbody T) per-degree)))))

(defn SVR
  "Systemic vascular resistance in dyn⋅s⋅cm⁻⁵"
  [Q MAP CVP]
  (* 80 (/ (- MAP CVP) Q)))

(def Hb->HCT "Convert between Hb in g/L and HCT in %" 0.3)

(defn C
  "Oxygen content in mL/L (normal ~200)"
  ; https://www.uptodate.com/contents/oxygen-delivery-and-consumption
  [Hb SO2 PO2]
  (let [O2-per-gram-Hb 1.34
        Hb' (/ Hb 10.) ; convert to g/dL
        SO2' (/ SO2 100) ; convert to proportion
        CO2-per-dL (+ (* O2-per-gram-Hb Hb' SO2')
                      (* 0.0031 PO2))
        dL-per-L 10.]
    (* CO2-per-dL dL-per-L)))

(defn DO2
  "Oxygen delivery in mL/min (normal ~1000)"
  [Q Hb SaO2 PaO2]
  (* Q (C Hb SaO2 PaO2)))

#_ (defn VO2
     "Oxygen consumption in mL/min (normal 200-250)"
     [Q SaO2 PaO2 SvO2 PvO2]
     (let [CaO2 (C Hb SaO2 PaO2)
           CvO2 (C Hb SvO2 PvO2)]
       (* Q (- CaO2 CvO2))))

#_ (defn ERO2
     "Oxygen extraction ratio (normal 0.22~0.3)"
     [Hb SaO2 PaO2 SvO2 PvO2]
     (let [CaO2 (C Hb SaO2 PaO2)
           CvO2 (C Hb SvO2 PvO2)]
       (/ (- CaO2 CvO2) CaO2)))

#_ (defn EIO2
     "Oxygen extraction index (normal 0.22-0.25)"
     [SaO2 SvO2]
     (/ (- SaO2 SvO2) SaO2))

(defn calcs [{:keys [Q MAP CVP SvO2 Hb T SaO2 PaO2 height weight]}]
  (let [BSA= (BSA height weight)
        SVR= (SVR Q MAP CVP)]
    {:BSA BSA=
     :QI (/ Q BSA=)
     :flow (/ Q 2.4)
     :flow-adj (/ Q (adjusted 2.4 T))
     :SVR SVR=
     :SVRI (/ SVR= BSA=)
     :HCT (* Hb Hb->HCT)
     :DO2 (DO2 Q Hb SaO2 PaO2)}))