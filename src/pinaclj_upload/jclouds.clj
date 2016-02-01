(ns pinaclj-upload.jclouds
  (:import (java.util Properties)
           (org.jclouds.openstack.keystone.v2_0.config CredentialTypes KeystoneProperties)
           (org.jclouds.openstack.swift.v1.blobstore RegionScopedBlobStoreContext)
           (org.jclouds ContextBuilder)
           (org.jclouds.logging.config ConsoleLoggingModule)
           (org.jclouds.rackspace.cloudfiles.v1 CloudFilesApi)
           (org.jclouds.io ContentMetadataBuilder Payload Payloads)
           (org.jclouds.io.payloads BaseMutableContentMetadata)))

(defprotocol Upload
  (put [this file-path input-stream content-type file-size]))

(def password-credentials
  (let [props (Properties.)]
    (.put props KeystoneProperties/CREDENTIAL_TYPE CredentialTypes/PASSWORD_CREDENTIALS)
    props))

(defn- add-credential-type [ctxt-builder password]
  (if password
    (.overrides ctxt-builder password-credentials)
    ctxt-builder))

(defn- build-api [{:keys [cloud username api-key password region]}]
  (-> (ContextBuilder/newBuilder cloud)
      (.credentials username (or api-key password))
      (add-credential-type password)
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
