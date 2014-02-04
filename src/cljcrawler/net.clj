(ns cljcrawler.net
  (:use [cljcrawler.config])
  (:use [cljcrawler.util])
  (:use [clojure.string :only (join)])
  (:import [java.util ArrayList])
  (:import [org.apache.http NameValuePair])
  (:import [org.apache.http.message BasicNameValuePair])
  (:import [org.apache.http.client.entity UrlEncodedFormEntity])
  (:import [org.apache.http Consts])
  (:import [org.jsoup Jsoup])
  (:import [org.apache.http.impl.client BasicCookieStore HttpClients LaxRedirectStrategy])
  (:import [org.apache.http.client.protocol HttpClientContext])
  (:import [org.apache.http.protocol ExecutionContext]))

(def http-client (promise))
(def local-context (promise))
(def start-url (promise))


(defn add-common-headers
  [client]
    (.addHeader client "User-Agent" "Mozilla/5.0 (X11; Linux x86_64; rv:17.0) Gecko/20130515 Firefox/17.0")
    (.addHeader client "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    (.addHeader client "Accept-Language", "en-US,en;q=0.5")
    (.addHeader client "Content-Type", "application/x-www-form-urlencoded"))

(defn combine-get-params
  [inputs]
  (join "&" (for [input inputs] (str (.attr input "name") "=" (.attr input "value")))))

(defn inputs-to-pair-list
 [arr extra]
 (let [len (count arr) nvps (new ArrayList)]
   (loop [n 0]
     (when (< n len)
       (let [input (clojure.core/aget arr n) name (.attr input "name") value (.attr input "value")]
         (if (contains? extra name)
           (.add nvps (BasicNameValuePair. name (get extra name)))
           (.add nvps (BasicNameValuePair. name value)))
         )
       (recur (inc n))))
   nvps))


(defn get-url
  [httpclient context url]
  (let [getter (new org.apache.http.client.methods.HttpGet url)]
    (add-common-headers getter)
    (.execute httpclient getter context)))

(defn post-url
  [httpclient context url inputs & kvs]
  (let [poster (new org.apache.http.client.methods.HttpPost url) extra (apply hash-map kvs)]
    (add-common-headers poster)
    (.setEntity poster (UrlEncodedFormEntity. (inputs-to-pair-list (.toArray inputs) extra) Consts/UTF_8))
    (.execute httpclient poster context)))

(defn get-current-uri
  [resp ctx]
  (.toString (.getURI (.getAttribute ctx (ExecutionContext/HTTP_REQUEST)))))

(defn prepare-http-conn
  []
  (let [cookie-store (BasicCookieStore.)
        the-context (HttpClientContext/create)
        the-client (.build (.setRedirectStrategy (HttpClients/custom) (LaxRedirectStrategy.)))]
    (println "Preparing http connection...")
    (.setCookieStore the-context cookie-store)
    (deliver start-url conf-start-url)
    (deliver http-client the-client)
    (deliver local-context the-context)))
