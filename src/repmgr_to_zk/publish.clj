(ns repmgr-to-zk.publish
  (:require [clojure.tools.logging :as log]
            [repmgr-to-zk.config :as config]
            [repmgr-to-zk.repmgr :as repmgr]
            [repmgr-to-zk.zk :as zk]))

;; (def processes
;;   (-> check-if-accurate
;;       check-if-new))

;; (check latest-master latest-promoted)
;; (predicate)
;; (1. retry)

;; (2. read-zk)
;; (predicate)
;; (1. publish)
;; (2. exit)

(defn- check-if-accurate []
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

(defn- check-if-new [master-data zk-data]
  (>= (compare (:event-timestamp master-data)
               (:event-timestamp zk-data))
      0))

(defn check-and-update-status []
  (log/debug "Checking before publishing new status")
  (if-let [master-data (check-if-accurate)]
    (try
      (zk/set-data master-data (partial check-if-new master-data))
      (catch Exception e
        (log/error e "Unable to publish status.")))))
