(ns pinaclj-upload.core
  (:gen-class)
  (:require [pinaclj-upload.jclouds :as jclouds]
            [pinaclj.nio :as nio]
            [pinaclj.files :as files]
            [pantomime.mime :as mime])
  (:import (java.nio.file Files)))

(def site-definition-file "site.clj")
(def pages-directory "pages")

(defn- read-config [directory]
  (clojure.edn/read-string (apply str (nio/read-all-lines (nio/resolve-path directory site-definition-file)))))

(defn put-all [api root-fs files]
  (reduce
    (fn [api file-path]
      (let [upload-path (str (nio/relativize root-fs file-path))]
        (with-open [fs (nio/input-stream file-path)]
          (println "Putting" (str file-path))
          (jclouds/put api
                       upload-path
                       fs
                       (mime/mime-type-of (str file-path))
                       (Files/size file-path)))))
    api files))

(defn run [api-fn directory]
  (let [pages-directory (nio/resolve-path directory pages-directory)]
    (put-all (api-fn (read-config directory))
             pages-directory
             (files/all-in pages-directory))))

(def current-directory (System/getProperty "user.dir"))

(defn -main [& args]
  (run jclouds/make (nio/resolve-path (files/init-default) current-directory)))
