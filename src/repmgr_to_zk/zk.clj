(ns repmgr-to-zk.zk
  (:require [clojure.string :as s]
            [repmgr-to-zk.config :as config]
            [zookeeper :as zk]
            [zookeeper.data :as zk-data]))

(defonce ^ThreadLocal ^{:private true}
  client (ThreadLocal.))

(def ^:const max-tries (or 3 (config/lookup :max-tries)))

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

(defn retry
  [timeout-msec fn & args]
  (loop [c @client]
    (if client
      (let [result (try (apply fn client args)
                        (catch KeeperException$SessionExpiredException)
                        (catch KeeperException$ConnectionLossException))]
        (if (= ::retry result)
          (recur (init!))
          result)))
    (throw (ex-info "Could not connect in time to Zookeeper for retry of function."))))

(defn compare-and-set
  [client timeout path f & args]
  (let [new-data (try (let [data (zk/data client path)
                            deserialized (when-let [bytes (:data data)]
                                           (*deserializer* bytes))
                            new-data (apply f deserialized args)
                            version (-> data :stat :version)]
                        (zk/set-data client path (*serializer* new-data) version)
                        new-data)
                      (catch KeeperException$BadVersionException bve ::retry))]))

(defn init! []
  (.set client (zk/connect (config/lookup :zookeeper :connect)))
  (alter-var-root #'client
                  (constantly (zk/connect (config/lookup :zookeeper :connect)))))

(defn destroy! []
  (zk/close client)
  (alter-var-root #'client nil))
