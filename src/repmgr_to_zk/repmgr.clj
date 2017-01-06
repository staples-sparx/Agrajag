(ns repmgr-to-zk.repmgr
  (:require [clojure.java.shell :as shell]
            [clojure.string :as s]
            [repmgr-to-zk.config :as config]
            [repmgr-to-zk.db :as db]))

(defn- parse-output [cluster-show-output]
  (for [line (drop 2 (s/split cluster-show-output #"\n"))
        :let [[role node-name upstream connection-string] (map s/trim (s/split line #"\|"))]]
    {:name node-name
     :role (re-find #"\w+" role)
     :upstream upstream
     :connection-string connection-string}))

(defn cluster-status []
  (let [config-file (config/lookup :repmgr :config-file)
        cluster-response (shell/sh "repmgr" "-f" config-file "cluster" "show")
        exit-status (:exit cluster-response)]
    (if (= 0 exit-status)
      (parse-output (:out cluster-response))
      (throw (ex-info "Failed to retrieve cluster status" cluster-response)))))

(defn master []
  (let [nodes (cluster-status)
        master-node (->> nodes (filter #(= "master" (:role %))) first)]
    (if (some? master-node)
      (:name master-node)
      (throw (ex-info "No master node." nodes)))))

(defn latest-promoted-standby []
  (let [event "SELECT node_id, event_timestamp
               FROM repl_events
               WHERE event = 'standby_promote' AND successful = 't'
               ORDER BY event_timestamp DESC LIMIT 1;"]
    (db/with-read-only-connection conn
      (db/q conn event))))
