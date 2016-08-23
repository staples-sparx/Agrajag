(ns repmgr-to-zk.core
  (:require [cider.nrepl :as cider]
            [refactor-nrepl.middleware :as refactor-nrepl]
            [clojure.tools.nrepl.server :as nrepl]
            [repmgr-to-zk.util :as util]
            [repmgr-to-zk.repmgr :as repmgr]
            [repmgr-to-zk.zk :as zk]
            [clojure.tools.logging :as log]))

(defonce instance
  {:thread-pool nil
   :zk-client nil})

(defn- start-nrepl! []
  (let [port 18001]
    (log/info "Starting nREPL server on port" port)
    (nrepl/start-server
     :port port
     :handler (-> cider/cider-nrepl-handler refactor-nrepl/wrap-refactor))))

(defn publish-status []
  (log/debug "Publishing status")
  (try
    (when-let [master-ip (repmgr/master)]
      (zk/set-master (:zk-client instance) master-ip))
    (catch Exception e
      (log/error e "Unable to publish status."))))

(defn stop! []
  (log/info "stopping!")
  (when (:thread-pool instance)
    (util/stop-tp (:thread-pool instance)))
  (zk/close-client (:zk-client instance))
  (nrepl/stop-server (:nrepl-server instance))
  nil)

(defn add-shutdown-hook []
  (let [shutdown-hook (Thread. stop!)
        runtime (Runtime/getRuntime)]
    (.addShutdownHook runtime shutdown-hook)))

(defn start! []
  (alter-var-root #'instance
                  (constantly
                   {:zk-client (zk/get-client)
                    :thread-pool (util/create-scheduled-tp publish-status 1000)
                    :nrepl-server (start-nrepl!)}))
  (add-shutdown-hook)
  (log/info "initialized!")
  (prn "initialized!")
  nil)

(defn -main [& _]
  (start!))

(comment
  (let [client (zk/connect "127.0.0.1:2182" :watcher (fn [event] (println event)))
        path "/eccentrica-db/master-ip"]
    (set client path "33.33.33.33")
    (get client path)))
