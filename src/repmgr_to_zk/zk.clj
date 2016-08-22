(ns repmgr-to-zk.zk
  (:require [zookeeper :as zk]
            [zookeeper.data :as zk-data]
            [clojure.string :as s]))


(defn get [client path]
  (zk-data/to-string (:data (zk/data client path))))

(defn create [client path]
  (let [paths (->> (s/split path #"/")
                   (reductions #(str %1 "/" %2))
                   (remove s/blank?))]
    (doseq [p paths]
      (zk/create client p :persistent? true))))

(defn set [client path master-ip]
  (let [version (:version (zk/exists client path))]
    (when-not (some? version)
      (create client path))
    (zk/set-data client
                 path
                 (zk-data/to-bytes master-ip)
                 version)))
