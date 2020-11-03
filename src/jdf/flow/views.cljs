(ns jdf.flow.views
  (:require [helix.core :refer [defnc $ <>]]))

(defnc root []
  (<> ($ :h1 "hello")))