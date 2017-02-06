(ns agrajag.monitoring
  (:require [clojure.tools.logging :as log]
            [agrajag.config :as config]
            [agrajag.repmgr :as repmgr]
            [wonko-client.collectors :as wc]
            [wonko-client.core :as wonko]))

(defn init! []
  (when (config/integrate-with-wonko?)
    (wonko/init! "agrajag"
                 (config/lookup :kafka-client)
                 :drop-on-reject? true)
    (wc/start-host-metrics)
    (log/info "Initialized monitoring!")))

(defn destroy! []
  (when (config/lookup :integrate-with-wonko?)
    (wonko/terminate!)
    (log/info "Stopped monitoring!")))

(defn- heartbeat []
  (wonko/counter :heartbeat {}))

(defn- cluster-status-metrics []
  (let [cluster (repmgr/nodes-in-cluster)]
    (for [type [:standby :failed :master]]
      (map #(wonko/counter type {:hostname (-> % :name)})
           (type cluster)))))

(defn metrics []
  (try
    (heartbeat)
    (cluster-status-metrics)
    (catch Exception e
      (log/error e "Unable to publish metrics"))))
