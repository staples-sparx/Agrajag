(ns agrajag.publish
  (:require [clojure.tools.logging :as log]
            [agrajag.config :as config]
            [agrajag.repmgr :as repmgr]
            [agrajag.zk :as zk]))

(defn- latest-cluster-status []
  (let [latest-promoted-standby (repmgr/latest-promoted-standby)
        latest-master (repmgr/latest-master)
        latest-cluster (repmgr/nodes-in-cluster)]
    (when (= (-> latest-promoted-standby
                 :node-id
                 repmgr/node-by-id
                 :name)
             latest-master)
      (assoc latest-promoted-standby
             :name latest-master
             :cluster latest-cluster))))

(defn- new-master? [master-data zk-data]
  (> (compare (:event-timestamp master-data)
              (:event-timestamp zk-data))
     0))

(defn- cluster-change? [new-cluster zk-cluster]
  (= new-cluster zk-cluster))

(defn update []
  (log/debug "Checking before publishing new status")
  (try
    (when-let [cluster-data (latest-cluster-status)]
      (let [master-data (dissoc cluster-data :cluster)
            failed-data (-> cluster-data :cluster :failed)
            standby-data (-> cluster-data :cluster :standby)]
        ;; TODO
        ;; The following calls to zk need to be in a transaction
        (zk/set-data master-data
                     (config/lookup :zookeeper :master-path)
                     (partial new-master? master-data))
        (zk/set-data failed-data
                     (config/lookup :zookeeper :failed-path)
                     (partial cluster-change? failed-data))
        (zk/set-data standby-data
                     (config/lookup :zookeeper :standby-path)
                     (partial cluster-change? standby-data))))
    (catch Exception e
      (log/error e "Unable to publish status."))))
