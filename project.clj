(defproject repmgr-to-zk "0.1.0"
  :local-repo ".m2"
  :description "Send repmgr cluster information to zookeeper."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [zookeeper-clj "0.9.4"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[cider/cider-nrepl "0.13.0"]
            [refactor-nrepl "2.2.0"]])
