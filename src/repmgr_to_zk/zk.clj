(ns repmgr-to-zk.zk
  (:require [zookeeper :as zk]
            [zookeeper.data :as zk-data]
            [clojure.string :as s]
            [repmgr-to-zk.config :as config]))

(defn close-client [client]
  (zk/close client))

(defn get-client []
  (zk/connect (config/lookup :zookeeper :connect)))

(defn get-master [client path]
  (zk-data/to-string (:data (zk/data client path))))

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
    (when-not (= (get-master client path)
                 master-ip)
      (zk/set-data client
                   path
                   (zk-data/to-bytes master-ip)
                   version))))
