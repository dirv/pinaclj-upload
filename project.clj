(defproject pinaclj-upload "0.1.0-SNAPSHOT"
  :description "Upload Pinaclj sites to cloud"
  :url "http://github.com/dirv/pinaclj-upload"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [pinaclj "0.1.0-SNAPSHOT"]
                 [org.apache.jclouds/jclouds-all "1.9.2"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"] ]}}
  :main pinaclj-upload.core
  :plugins [[speclj "3.3.1"]]
  :test-paths ["spec"])
