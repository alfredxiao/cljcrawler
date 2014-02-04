(ns cljcrawler.util
  (:use [cljcrawler.config])
  (:use [clojure.string :only (join)])
  (:import [org.apache.http.util EntityUtils])
  (:import [java.util ArrayList])
  (:import [java.io ByteArrayOutputStream])
  (:import [java.lang ProcessBuilder])
  (:import [org.jsoup Jsoup])
  (:import [org.apache.commons.io IOUtils]))

(defn gen-uuid 
  []
  (str (java.util.UUID/randomUUID)))

(def url-filename-map (atom {}))

(def index-url (promise))

(defn resp-as-string
  [resp]
  (EntityUtils/toString (.getEntity resp)))

(defn get-element
  [cont tag-name]
  (.select (Jsoup/parse cont) tag-name))


(defn make-complete-url
  [uri ref-base]
  (cond 
    (.startsWith uri "http://") uri
    (.startsWith uri "https://") uri
    (.startsWith uri "javascript:") uri
    (.startsWith uri "mailto:") uri
    (.startsWith uri "/") (str conf-site-base uri)
    :default (str (.substring ref-base 0 (.lastIndexOf ref-base "/")) (if (.startsWith uri "/") "" "/") uri)))

(defn get-file-type
  [fullpath]
  (let [pb (ProcessBuilder. ["/usr/bin/file" fullpath]) 
        out (ByteArrayOutputStream.)
        p (.start pb)]
    (IOUtils/copy (.getInputStream p) out)
    (.substring (.toString out "UTF-8") (+ 2 (.length fullpath)))))
