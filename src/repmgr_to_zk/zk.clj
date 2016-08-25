(ns repmgr-to-zk.zk
  (:require [clojure.string :as s]
            [repmgr-to-zk.config :as config]
            [zookeeper :as zk]
            [zookeeper.data :as zk-data]))

(defn close-client [client]
  (zk/close client))

(defn get-client []
  (zk/connect (config/lookup :zookeeper :connect)))

(defn create [client path]
  (let [paths (->> (s/split path #"/")
                   (reductions #(str %1 "/" %2))
                   (remove s/blank?))]
    (doseq [p paths]
      (zk/create client p :persistent? true))))

(defn set-master [client master-ip]
  (let [path (config/lookup :zookeeper :master-path)
        version (:version (zk/exists client path))]
    (when-not (some? version)
      (create client path))
    (zk/set-data client
                 path
                 (zk-data/to-bytes master-ip)
                 version)))
