(ns cli
  (:require [repmgr-to-zk.core :as launcher]))

(let [args (apply str (interpose " " *command-line-args*))]
  (apply launcher/-main *command-line-args*))
