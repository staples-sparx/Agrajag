(ns repmgr-to-zk.monitoring
  (:require [clojure.tools.logging :as log]
            [repmgr-to-zk.config :as config]
            [repmgr-to-zk.repmgr :as repmgr]
            [wonko-client.collectors :as wc]
            [wonko-client.core :as wonko]))

(defn init! []
  (when (config/lookup :integrate-with-wonko?)
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

(defn- cluster-status []
  (wonko/gauge :cluster-status [] (repmgr/nodes-in-cluster)))

(defn- master-db []
  (wonko/gauge :master-db [] (assoc (repmgr/latest-promoted-standby)
                                    :name (repmgr/latest-master))))

(defn metrics []
  (heartbeat)
  (cluster-status)
  (master-db))
