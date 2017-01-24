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

(defn- standby-cluster []
  (let [cluster (repmgr/nodes-in-cluster)]
    (doall (map #(wonko/counter :standby-cluster {:hostname (-> % :name)})
                (:standby cluster)))))

(defn- failed-cluster []
  (let [cluster (repmgr/nodes-in-cluster)]
    (doall (map #(wonko/counter :failed-cluster {:hostname (-> % :name)})
                (:failed cluster)))))

(defn- master-db []
  (let [master (repmgr/latest-master)]
    (wonko/counter :master-db {:hostname master})))

(defn metrics []
  (try
    (heartbeat)
    (standby-cluster)
    (failed-cluster)
    (master-db)
    (catch Exception e
      (log/error e "Unable to publish metrics"))))
