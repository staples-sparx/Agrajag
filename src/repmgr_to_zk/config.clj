(ns repmgr-to-zk.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defonce ^:private config-resource
  (-> "config.edn" io/resource))

(defn- read-config
  ([] (read-config config-resource))
  ([config] (-> config slurp edn/read-string)))

(defonce current (atom (read-config)))

(defn reload! []
  (reset! current (read-config)))

(defn lookup [& ks]
  (get-in @current ks))

;; (reload!)
