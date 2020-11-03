(ns user
  (:require [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api :as shadow]))

(server/start!)
(shadow/watch :app)

(let [port (-> (shadow/get-config) :dev-http ffirst)]
  (clojure.java.browse/browse-url (str "http://localhost:" port)))
; Doesn't actually work properly when run at toplevel as part of startup, even if browser is ready.
#_(shadow/repl :app)