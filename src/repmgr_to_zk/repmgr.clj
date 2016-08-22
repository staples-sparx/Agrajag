(ns repmgr-to-zk.repmgr
  (:require [clojure.java.jdbc :as j]
            [clojure.string :as s]
            [repmgr-to-zk.config :as config])
  (:import [java.sql SQLException]))

(def default-db-config
  {:dbtype "postgresql"
   :dbname "repmgr"
   :host "localhost"
   :user "repmgr"
   :password "repmgr"
   :port "5432"})

(def db
  (merge default-db-config (config/lookup :repmgr)))

(defn read-cluster-status []
  (let [suffix (config/lookup :repmgr :suffix)
        query (format "SELECT conninfo, type, name, upstream_node_name, id
                       FROM repmgr_%s.repl_show_nodes;"
                      suffix)]
    (j/query db query)))

;; TODO: this is not correct. we should be shelling out to repmgr cluster show --csv.
(defn master []
  (let [cluster-status (read-cluster-status)]
    (:name (first (filter #(= "master" (:type %)) cluster-status)))))
