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
  (let [cluster (repmgr/nodes-in-cluster)]
    (map #(wonko/counter :cluster-status {:hostname (-> % :name)})
         (:standby cluster))))

(defn- master-db []
  (let [master (repmgr/latest-master)]
    (wonko/counter :master-db {:hostname master})))

(defn metrics []
  (try
    (heartbeat)
    (cluster-status)
    (master-db)
    (catch Exception e
      (log/error e "Unable to publish metrics"))))
