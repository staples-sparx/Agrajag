(ns repmgr-to-zk.db
  (:require [hikari-cp.core :as hikari]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [repmgr-to-zk.config :as config]))

(def ^:private connection-pool (atom nil))

(defn init! []
  (reset!
   connection-pool
   {:datasource (hikari/make-datasource (config/lookup :db-spec))}))

(defn destroy! []
  (-> @connection-pool
      :datasource
      (hikari/close-datasource))
  (reset! connection-pool nil)
  (log/info "Destroyed connection pool"))

(defn get-connection [] @connection-pool)

(defmacro with-read-only-connection [conn & body]
  `(jdbc/with-db-connection
     [~conn (get-connection) :read-only? true]
     ~@body))

(defn- ->hyphens [^String x]
  (keyword (.replace x \_ \-)))

(defn q [conn query]
  (doall (jdbc/query conn
                     query
                     {:identifiers ->hyphens})))
