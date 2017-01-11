(ns repmgr-to-zk.zk
  (:require [clojure.string :as s]
            [repmgr-to-zk.config :as config]
            [zookeeper :as zk]
            [zookeeper.data :as zk-data]))

(defonce client nil)

(defn get-data [client path]
  (-> (zk/data client path)
      :data
      zk-data/to-string
      read-string))

(defn create-path [client path]
  (let [paths (->> (s/split path #"/")
                   (reductions #(str %1 "/" %2))
                   (remove s/blank?))]
    (doseq [p paths]
      (zk/create client p :persistent? true))))

(defn set-master [client master-ip]
  (let [path (config/lookup :zookeeper :master-path)
        version (:version (zk/exists client path))]
    (when-not (some? version)
      (create-path client path))
    (zk/set-data client
                 path
                 (zk-data/to-bytes (pr-str master-ip))
                 version)))

(defn init! []
  (alter-var-root #'client
                  (constantly (zk/connect (config/lookup :zookeeper :connect)))))

(defn destroy! []
  (zk/close client)
  (alter-var-root #'client nil))
