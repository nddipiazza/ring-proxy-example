(ns ring-proxy-example.proxy
    (:import
      [java.net URI] )
    (:require
      [clj-http.client         :refer [request]]
      [clj-http.conn-mgr       :as conn]
      [clojure.string          :refer [join split]]
      [ring.adapter.jetty      :refer [run-jetty]] ))

(defn slurp-binary
  "Reads len bytes from InputStream is and returns a byte array."
  [^java.io.InputStream is len]
  (with-open [rdr is]
    (let [buf (byte-array len)]
      (.read rdr buf)
      buf)))

(def reusable-conn-mgr (delay (conn/make-reusable-conn-manager {:timeout 200 :threads 50 :default-per-route 50 })))
(def http-client (atom nil))

(defn wrap-proxy
  "Proxies requests from proxied-path, a local URI, to the remote URI at
  remote-base-uri, also a string."
  [handler ^String proxied-path remote-base-uri & [http-opts]]
 (fn [req]
   (if (.startsWith ^String (:uri req) proxied-path)
     (let [rmt-full   (URI. (str remote-base-uri "/"))
           rmt-path   (URI. (.getScheme    rmt-full)
                            (.getAuthority rmt-full)
                            (.getPath      rmt-full) nil nil)
           lcl-path   (URI. (subs (:uri req) (.length proxied-path)))
           remote-uri (.resolve rmt-path lcl-path) ]
       (-> (merge {:method (:request-method req)
                   :url (str remote-uri "?" (:query-string req))
                   :headers (dissoc (:headers req) "host" "content-length")
                   :http-client @http-client
                   :connection-manager @reusable-conn-mgr
                   :body (if-let [len (get-in req [:headers "content-length"])]
                           (slurp-binary (:body req) (Integer/parseInt len)))
                   :follow-redirects true
                   :throw-exceptions false
                   :as :stream} http-opts)
           request))
     (handler req))))

(defn run-proxy
  [listen-path listen-port remote-uri http-opts]
  (-> (constantly {:status 404 :headers {} :body "404 - not found"})
      (wrap-proxy listen-path remote-uri http-opts)
      (run-jetty {:port listen-port}) ))