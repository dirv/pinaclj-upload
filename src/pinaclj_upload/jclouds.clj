(ns pinaclj-upload.jclouds
  (:import (org.jclouds.openstack.swift.v1.blobstore RegionScopedBlobStoreContext)
           (org.jclouds ContextBuilder)
           (org.jclouds.logging.config ConsoleLoggingModule)
           (org.jclouds.rackspace.cloudfiles.v1 CloudFilesApi)
           (org.jclouds.io Payload Payloads)))

(defprotocol Upload
  (put [this file-path input-stream]))

(defn build-api [{:keys [cloud username api-key region]}]
  (-> (ContextBuilder/newBuilder cloud)
      (.credentials username api-key)
      (.modules [(ConsoleLoggingModule.)])
      (.buildView RegionScopedBlobStoreContext)
      (.getBlobStore region)
      (.getContext)
      (.unwrapApi CloudFilesApi)))

(defn object-api [api {:keys [region container]}]
  (.getObjectApi api region container))

(deftype JClouds [api object-api]
  Upload
  (put [this file-path input-stream]
       (.put object-api file-path (Payloads/newInputStreamPayload input-stream))
       this)
  java.io.Closeable
  (close [this] (.close api)))

(defn make [config]
  (let [api (build-api config)]
    (->JClouds api (object-api api config))))
