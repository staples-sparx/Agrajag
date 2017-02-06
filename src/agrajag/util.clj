(ns agrajag.util
  (:import [java.util.concurrent Executors ThreadPoolExecutor TimeUnit]))

(defn create-scheduled-tp [f rate]
  (doto (Executors/newScheduledThreadPool 1)
    (.scheduleWithFixedDelay f 0 rate TimeUnit/MILLISECONDS)))

(defn stop-tp [^ThreadPoolExecutor tp]
  (when tp
    (.shutdownNow tp)))
