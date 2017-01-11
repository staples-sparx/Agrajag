(ns repmgr-to-zk.core
  (:require [cider.nrepl :as cider]
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [refactor-nrepl.middleware :as refactor-nrepl]
            [repmgr-to-zk.config :as config]
            [repmgr-to-zk.publish :as publish]
            [repmgr-to-zk.repmgr :as repmgr]
            [repmgr-to-zk.util :as util]
            [repmgr-to-zk.zk :as zk]
            [repmgr-to-zk.db :as db]))

(defonce instance
  {:thread-pool nil :zk-client nil})

(defn- start-nrepl! []
  (let [port 18001]
    (log/info "Starting nREPL server on port" port)
    (nrepl/start-server
     :port port
     :handler (-> cider/cider-nrepl-handler refactor-nrepl/wrap-refactor))))

(defn stop! []
  (log/info "stopping!")
  (when (:thread-pool instance)
    (util/stop-tp (:thread-pool instance)))
  (zk/close-client (:zk-client instance))
  (nrepl/stop-server (:nrepl-server instance))
  nil)

(defn add-shutdown-hook []
  (let [shutdown-hook (Thread. stop!)
        runtime (Runtime/getRuntime)]
    (.addShutdownHook runtime shutdown-hook)))

(defn start! []
  (alter-var-root #'instance
                  (constantly
                   {:zk-client (zk/get-client)
                    :thread-pool (util/create-scheduled-tp publish/status (config/lookup :frequency-ms))
                    :nrepl-server (start-nrepl!)}))
  (db/init!)
  (add-shutdown-hook)
  (log/info "initialized!")
  nil)

(defn -main [& _]
  (start!))

(comment
  (let [ip "kafka-1-perf.staples.com:2181"
        client (zk/connect ip :watcher (fn [event] (println event)))
        path "/eccentrica-api-db/master-ip"]
    (get client path)))
