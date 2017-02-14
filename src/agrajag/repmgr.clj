(ns agrajag.repmgr
  (:require [clojure.java.shell :as shell]
            [clojure.string :as s]
            [agrajag.config :as config]
            [agrajag.db :as db]))

(def repmgr-env (str "repmgr_" (config/lookup :env)))

(def ^:private node-state ["master" "failed" "standby"])

(defn- parse-output [cluster-show-output]
  (for [line (drop 2 (s/split cluster-show-output #"\n"))
        :let [[role node-name upstream connection-string] (map s/trim (s/split line #"\|"))]]
    {:name node-name
     :role (re-find #"\w+" role)
     :upstream upstream
     :connection-string connection-string}))

(defn cluster-status []
  (let [config-file (config/lookup :repmgr :config-file)
        cluster-response (shell/sh "repmgr" "-f" config-file "cluster" "show" "--log-to-file")
        exit-status (:exit cluster-response)]
    (if (= 0 exit-status)
      (parse-output (:out cluster-response))
      (throw (ex-info "Failed to retrieve cluster status" cluster-response)))))

(defn latest-master []
  (let [nodes (cluster-status)
        master-node (->> nodes (filter #(= "master" (:role %))) first)]
    (if (some? master-node)
      (:name master-node)
      (throw (ex-info "No master node." nodes)))))

(defn latest-promoted-standby []
  (let [event (str "SELECT node_id, event_timestamp FROM "
                   repmgr-env
                   ".repl_events WHERE event = 'standby_promote'
                    AND successful = 't'
                    ORDER BY event_timestamp DESC LIMIT 1;")]
    (db/with-read-only-connection conn
      (first (db/q conn event)))))

(defn node-by-id [^Integer id]
  (let [event [(str "SELECT id, name FROM "
                    repmgr-env
                    ".repl_nodes WHERE id = ?;") id]]
    (db/with-read-only-connection conn
      (first (db/q conn event)))))

(defn nodes-in-cluster []
  (let [cluster (cluster-status)
        cluster-map-list (for [role node-state
                               :let [cluster-map {}
                                     filtered-cluster (filter #(= role
                                                                  (-> (:role %)
                                                                      s/lower-case))
                                                              cluster)]]
                           (->> filtered-cluster
                                (map #(dissoc % :role :upstream :connection-string))
                                (assoc cluster-map (keyword role))))]
    (apply merge cluster-map-list)))
