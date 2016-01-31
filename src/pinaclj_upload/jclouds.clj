(ns pinaclj-upload.jclouds
  (:import (org.jclouds.openstack.swift.v1.blobstore RegionScopedBlobStoreContext)
           (org.jclouds ContextBuilder)
           (org.jclouds.logging.config ConsoleLoggingModule)
           (org.jclouds.rackspace.cloudfiles.v1 CloudFilesApi)
           (org.jclouds.io ContentMetadataBuilder Payload Payloads)
           (org.jclouds.io.payloads BaseMutableContentMetadata)))

(defprotocol Upload
  (put [this file-path input-stream content-type file-size]))

(defn- build-api [{:keys [cloud username api-key region]}]
  (-> (ContextBuilder/newBuilder cloud)
      (.credentials username api-key)
      (.modules [(ConsoleLoggingModule.)])
      (.buildView RegionScopedBlobStoreContext)
      (.getBlobStore region)
      (.getContext)
      (.unwrapApi CloudFilesApi)))

(defn- object-api [api {:keys [region container]}]
  (.getObjectApi api region container))

(defn- build-content-metadata [content-type file-size]
  (-> (ContentMetadataBuilder.)
      (.contentLength file-size)
      (.contentType content-type)
      (.build)
      (BaseMutableContentMetadata/fromContentMetadata)))

(deftype JClouds [api object-api]
  Upload
  (put [this file-path input-stream content-type file-size]
       (let [payload (Payloads/newInputStreamPayload input-stream)]
         (.setContentMetadata payload (build-content-metadata content-type file-size))
         (.put object-api file-path payload))
       this)
  java.io.Closeable
  (close [this] (.close api)))

(defn make [config]
  (let [api (build-api config)]
    (->JClouds api (object-api api config))))
