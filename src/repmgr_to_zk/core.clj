(ns repmgr-to-zk.core
  (:require [cider.nrepl :as cider]
            [refactor-nrepl.middleware :as refactor-nrepl]
            [clojure.tools.nrepl.server :as nrepl]
            [repmgr-to-zk.util :as util]
            [clojure.tools.logging :as log]))

(defonce thread-pool nil)

(defn- start-nrepl! []
  (let [port 18001]
    (nrepl/start-server
     :port port
     :handler (-> cider/cider-nrepl-handler refactor-nrepl/wrap-refactor))
    (log/info "Started nREPL server on port" port)))

(defn publish-status []
  (log/info "Publishing status"))

(defn stop! []
  (log/info "stopping!")
  (when thread-pool
    (util/stop-tp thread-pool)))

(defn add-shutdown-hook []
  (let [shutdown-hook (Thread. stop!)
        runtime (Runtime/getRuntime)]
    (.addShutdownHook runtime shutdown-hook)))

(defn start! []
  (alter-var-root #'thread-pool
                  (constantly
                   (util/create-scheduled-tp publish-status 1000)))
  (start-nrepl!)
  (add-shutdown-hook)
  (log/info "initialized!")
  (prn "initialized!")
  nil)

(defn -main [& _]
  (start!))

(comment
  (let [client (zk/connect "127.0.0.1:2182" :watcher (fn [event] (println event)))
        path "/eccentrica-db/master-ip"]
    (set client path "33.33.33.33")
    (get client path)))
