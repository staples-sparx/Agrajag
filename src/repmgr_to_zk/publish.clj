(ns repmgr-to-zk.publish
  (:require [clojure.tools.logging :as log]
            [repmgr-to-zk.config :as config]
            [repmgr-to-zk.core :as core]
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

(defn check-if-accurate []
  (let [latest-promoted-standby (repmgr/latest-promoted-standby)
        latest-master (repmgr/latest-master)]
    (when  (= (-> latest-promoted-standby
                  :node-id
                  repmgr/node-by-id
                  :name)
              latest-master)
      (assoc latest-promoted-standby :name latest-master))))

(defn check-if-new [master-data]
  (let [zk-client (:zk-client core/instance)
        zk-path (config/lookup :zookeeper :master-path)
        zk-data (zk/get-data zk-client zk-path)]
    (> (:event-timestamp master-data)
       (:event-timestamp zk-data))))

(defn status []
  (log/debug "Checking before publishing new status")
  (if-let [master-data (check-if-accurate)]
    (when (check-if-new master-data)
      (log/debug "Publishing new status")
      (try
        (zk/set-master (:zk-client core/instance) master-data)
        (catch Exception e
          (log/error e "Unable to publish status."))))))
