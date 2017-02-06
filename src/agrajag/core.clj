(ns agrajag.core
  (:require [cider.nrepl :as cider]
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [refactor-nrepl.middleware :as refactor-nrepl]
            [agrajag.config :as config]
            [agrajag.publish :as publish]
            [agrajag.repmgr :as repmgr]
            [agrajag.monitoring :as monitoring]
            [agrajag.util :as util]
            [agrajag.zk :as zk]
            [agrajag.db :as db]))

(defonce instance
  {:publishing-tpool nil
   :monitoring-tpool nil
   :nrepl-server nil})

(defn- start-nrepl! []
  (let [port 18001]
    (log/info "Starting nREPL server on port" port)
    (nrepl/start-server
     :port port
     :handler (-> cider/cider-nrepl-handler refactor-nrepl/wrap-refactor))))

(defn stop! []
  (log/info "stopping!")
  (when (:publishing-tpool instance)
    (util/stop-tp (:publishing-tpool instance)))
  (when (:monitoring-tpool instance)
    (util/stop-tp (:monitoring-tpool instance)))
  (zk/destroy!)
  (db/destroy!)
  (monitoring/destroy!)
  (nrepl/stop-server (:nrepl-server instance))
  nil)

(defn add-shutdown-hook []
  (let [shutdown-hook (Thread. stop!)
        runtime (Runtime/getRuntime)]
    (.addShutdownHook runtime shutdown-hook)))

(defn start! []
  (zk/init!)
  (db/init!)
  (monitoring/init!)
  (add-shutdown-hook)
  (alter-var-root #'instance
                  (constantly
                   {:publishing-tpool (util/create-scheduled-tp publish/update (config/lookup :frequency-ms))
                    :monitoring-tpool (util/create-scheduled-tp monitoring/metrics (config/lookup :frequency-ms))
                    :nrepl-server (start-nrepl!)}))
  (log/info "Initialized!")
  nil)

(defn -main [& _]
  (start!))

(comment
  (let [ip "kafka-1-perf.staples.com:2181"
        client (zk/connect ip :watcher (fn [event] (println event)))
        path "/eccentrica-api-db/master-ip"]
    (get client path)))
