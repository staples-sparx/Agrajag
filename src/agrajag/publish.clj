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
  (>= (compare (:event-timestamp master-data)
               (:event-timestamp zk-data))
      0))

(defn update []
  (log/debug "Checking before publishing new status")
  (try
    (when-let [master-data (latest-cluster-status)]
      (zk/set-data master-data (partial new-master? master-data)))
    (catch Exception e
      (log/error e "Unable to publish status."))))
