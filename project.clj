(defproject repmgr-to-zk "0.1.0"
  :local-repo ".m2"
  :repositories {"releases" {:url "s3p://runa-maven/releases/"
                             :username [:gpg :env/archiva_username]
                             :passphrase [:gpg :env/archiva_passphrase]}
                 "nuition" "http://maven.nuiton.org/release"}
  :description "Send repmgr cluster information to zookeeper."
  :license {:name "MIT"
            :url "http://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [zookeeper-clj "0.9.4"]
                 [org.clojure/java.jdbc "0.7.0-alpha1"]
                 [postgresql "9.3-1102.jdbc41"]
                 [hikari-cp "1.7.5"]
                 [staples-sparx/wonko-client "0.1.7"]]
  :jvm-opts ["-Xmx128m" "-Xms128m" "-server"]
  :plugins [[cider/cider-nrepl "0.13.0"]
            [refactor-nrepl "2.2.0"]])
