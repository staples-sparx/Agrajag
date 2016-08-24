(ns repmgr-to-zk.repmgr
  (:require [clojure.java.shell :as shell]
            [clojure.string :as s]
            [repmgr-to-zk.config :as config]))

(defn parse-output [cluster-show-output]
  (for [line (drop 2 (s/split cluster-show-output #"\n"))
        :let [[role node-name upstream connection-string] (map s/trim (s/split line #"\|"))]]
    {:name node-name
     :role (re-find #"\w+" role)
     :upstream upstream
     :connection-string connection-string}))

(defn cluster-status []
  (let [config-file (config/lookup :repmgr :config-file)
        cluster-show-output (:out (shell/sh "repmgr" "-f" config-file "cluster" "show"))]
    (parse-output cluster-show-output)))

(defn master []
  (->> (cluster-status)
       (filter #(= "master" (:role %)))
       first
       :name))
