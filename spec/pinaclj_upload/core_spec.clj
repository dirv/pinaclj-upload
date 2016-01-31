(ns pinaclj-upload.core-spec
  (:require [speclj.core :refer :all]
            [pinaclj.test-fs :as files]
            [pinaclj-upload.jclouds :as jclouds]
            [pinaclj-upload.core :refer :all])
  (:import pinaclj_upload.jclouds.Upload))

(defrecord UploadSpy [])

(extend-type UploadSpy
  jclouds/Upload
  (jclouds/put [this file-path input-stream]
       (assoc this file-path (slurp input-stream))))

(def all-files
  [{:path "1" :content "Test"}
   {:path "2" :content ""}
   {:path "3" :content ""}])

(describe :upload

  (with fs (files/create-from all-files))
  (with api (->UploadSpy))

  (it "puts all files"
    (should== ["1" "2" "3"] (keys (put-all @api @fs ["1" "2" "3"]))))
  (it "puts file content"
    (should== {"1" "Test"} (put-all @api @fs ["1"]))))
