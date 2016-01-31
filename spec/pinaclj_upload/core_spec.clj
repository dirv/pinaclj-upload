(ns pinaclj-upload.core-spec
  (:require [speclj.core :refer :all]
            [pinaclj.test-fs :as files]
            [pinaclj-upload.jclouds :as jclouds]
            [pinaclj-upload.core :refer :all])
  (:import pinaclj_upload.jclouds.Upload))

(defrecord UploadSpy [config])

(extend-type UploadSpy
  jclouds/Upload
  (jclouds/put [this file-path input-stream]
       (assoc-in this [:put file-path] (slurp input-stream))))

(def all-files
  [{:path "site.clj" :content "{:a 1 :b 2}"}
   {:path "pages/1" :content "Test"}
   {:path "pages/2" :content ""}
   {:path "pages/3" :content ""}])

(describe :run
  (with fs (files/create-from all-files))
  (with api-fn ->UploadSpy)

  (it "puts all files"
    (should== ["1" "2" "3"] (keys (:put (run @api-fn @fs)))))
  (it "puts file content"
    (should= "Test" (get (:put (run @api-fn @fs)) "1")))
  (it "reads config"
    (should= {:a 1 :b 2} (:config (run @api-fn @fs)))))
