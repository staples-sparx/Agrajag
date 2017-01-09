(ns repmgr-to-zk.publish
  (:require [repmgr-to-zk.repmgr :as repmgr]
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
  (=  (-> repmgr/latest-promoted-standby
          :node-id
          repmgr/node-by-id
          :name)
      (repmgr/latest-master)))

(defn check-if-new [to-write already-written])

(defn status []
  (log/debug "Publishing status")
  (try
    (zk/set-master (:zk-client instance) (repmgr/master))
    (catch Exception e
      (log/error e "Unable to publish status."))))
