(ns repmgr-to-zk.db
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [repmgr-to-zk.config :as config]))

(def ^:private db-config (config/lookup :database))
(def ^:private db-spec
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname (str "//" (:host db-config) ":" (:port db-config)"/" (:name db-config))
   :user (:username db-config)
   :password (:password db-config)})

(defmacro with-read-only-connection [conn & body]
  `(jdbc/with-db-connection
     [~conn db-spec :read-only? true]
     ~@body))

(defn- ->hyphens [^String x]
  (keyword (.replace x \_ \-)))

(defn q [conn query]
  (doall (jdbc/query conn
                     query
                     {:identifiers ->hyphens})))
