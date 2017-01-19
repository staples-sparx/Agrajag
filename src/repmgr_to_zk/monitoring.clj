(ns repmgr-to-zk.monitoring
  (:require [clojure.tools.logging :as log]
            [repmgr-to-zk.config :as config]
            [wonko-client.collectors :as wc]
            [wonko-client.core :as wonko]))

(defn init! []
  (when (config/lookup :integrate-with-wonko?)
    (wonko/init! "agrajag"
                 (config/lookup :kafka-client)
                 :drop-on-reject? true)
    (wc/start-host-metrics)
    (wc/start-ping)
    (log/info "Initialized monitoring!")))

(defn destroy! []
  (when (config/lookup :integrate-with-wonko?)
    (wonko/terminate!)
    (log/info "Stopped monitoring!")))
