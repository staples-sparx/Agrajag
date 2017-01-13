(ns repmgr-to-zk.zk
  (:require [clojure.string :as s]
            [repmgr-to-zk.config :as config]
            [zookeeper :as zk]
            [zookeeper.data :as zk-data])
  (:import (org.apache.zookeeper KeeperException$BadVersionException
                                 KeeperException$SessionExpiredException
                                 KeeperException$ConnectionLossException)))

(defonce ^{:private true} client nil)

(defn- deserializer [data]
  (-> data
      zk-data/to-string
      read-string))

(defn- serializer [data]
  (-> data
      pr-str
      zk-data/to-bytes))

(def ^:const max-tries (or 3 (config/lookup :max-tries)))

(defn init! []
  (alter-var-root #'client
                  (constantly (zk/connect (config/lookup :zookeeper :connect)))))

(defn destroy! []
  (zk/close client)
  (alter-var-root #'client nil))

(defn- retry
  [fn & args]
  (loop [count max-tries]
    (if (> count 0)
      (let [result (try (apply fn client args)
                        (catch KeeperException$SessionExpiredException see ::reinit)
                        (catch KeeperException$ConnectionLossException cle ::reinit))]
        (condp = result
          ::reinit (do (init!)
                       (recur (dec count)))
          result))
      (throw (Exception. "Failed to connect to Zookeeper")))))

(defn- compare-and-set
  [client path predicate? new-data & args]
  (try (let [zk-data (zk/data client path)
             deserialized (when-let [bytes (:data zk-data)]
                            (deserializer bytes))
             version (-> zk-data :stat :version)]
         (if (predicate? deserialized)
           (zk/set-data client path (serializer new-data) version)))
       (catch KeeperException$BadVersionException bve)))

(defn get-data [path]
  (retry (fn [client path]
           (-> (zk/data client path)
               :data
               zk-data/to-string
               read-string))
         path))

(defn create-path [path]
  (retry
   (fn [client path]
     (let [paths (->> (s/split path #"/")
                      (reductions #(str %1 "/" %2))
                      (remove s/blank?))]
       (doseq [p paths]
         (zk/create client p :persistent? true))))
   path))

(defn set-data [data predicate?]
  (let [path (config/lookup :zookeeper :master-path)
        version (:version (zk/exists client path))]
    (when-not (some? version)
      (create-path client path))
    (retry compare-and-set
           path
           predicate?
           data)))
