(ns user
  (:require [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]))

(defn go []
  (server/start!)
  (shadow/watch :app)

  (let [port (-> (shadow/get-config) :dev-http ffirst)]
    (clojure.java.browse/browse-url (str "http://localhost:" port)))

  (shadow/repl :app))

#_ (go)