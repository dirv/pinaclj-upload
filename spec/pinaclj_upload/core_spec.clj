(ns pinaclj-upload.core-spec
  (:require [speclj.core :refer :all]
            [pinaclj.test-fs :as files]
            [pinaclj-upload.jclouds :as jclouds]
            [pinaclj-upload.core :refer :all])
  (:import pinaclj_upload.jclouds.Upload))

(defrecord UploadSpy [config])

(extend-type UploadSpy
  jclouds/Upload
  (jclouds/put [this file-path input-stream content-type file-size]
       (assoc-in this [:put file-path] [(slurp input-stream) content-type file-size])))

(def all-files
  [{:path "site.clj" :content "{:a 1 :b 2}"}
   {:path "pages/1" :content "Test"}
   {:path "pages/2.html" :content ""}
   {:path "pages/3.png" :content ""}])

(describe :run
  (with fs (files/create-from all-files))
  (with api-fn ->UploadSpy)

  (it "puts all files"
    (should== ["1" "2.html" "3.png"] (keys (:put (run @api-fn @fs)))))
  (it "puts file content"
    (should= "Test" (first (get (:put (run @api-fn @fs)) "1"))))
  (it "sets the appropriate file type based on the extension"
    (should= "text/html" (second (get (:put (run @api-fn @fs)) "2.html")))
    (should= "image/png" (second (get (:put (run @api-fn @fs)) "3.png"))))
  (it "sets the appropriate file size"
    (should= 4 (last (get (:put (run @api-fn @fs)) "1"))))
  (it "reads config"
    (should= {:a 1 :b 2} (:config (run @api-fn @fs)))))
