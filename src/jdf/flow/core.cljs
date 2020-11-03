(ns jdf.flow.core
  (:require ["react-dom" :refer [render]]
            [jdf.flow.views :as views]
            [helix.core :refer [$]]))

(defn ^:dev/after-load mount-root []
  (render ($ views/root) (. js/document getElementById "app")))

(defn ^:export init []
  (mount-root))