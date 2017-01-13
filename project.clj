(defproject repmgr-to-zk "0.1.0"
  :local-repo ".m2"
  :description "Send repmgr cluster information to zookeeper."
  :license {:name "MIT"
            :url "http://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [zookeeper-clj "0.9.4"]
                 [org.clojure/java.jdbc "0.7.0-alpha1"]
                 [postgresql "9.3-1102.jdbc41"]
                 [hikari-cp "1.7.5"]]
  :jvm-opts ["-Dcom.sun.management.jmxremote"
             "-Dcom.sun.management.jmxremote.ssl=false"
             "-Dcom.sun.management.jmxremote.authenticate=false"
             "-Dcom.sun.management.jmxremote.port=43210"
             "-Xmx128m"
             "-Xms128m"
             "-server"]
  :plugins [[cider/cider-nrepl "0.13.0"]
            [refactor-nrepl "2.2.0"]])
