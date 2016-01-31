(ns pinaclj-upload.core
  (:require [pinaclj-upload.jclouds :as jclouds]
            [pinaclj.nio :as nio]))

(defn put-all [api root-fs files]
  (reduce
    (fn [api file]
      (with-open [fs (nio/input-stream (nio/resolve-path root-fs file))]
        (jclouds/put api file fs)))
    api files))
