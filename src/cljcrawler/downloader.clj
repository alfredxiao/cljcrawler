(ns cljcrawler.downloader
  (:use [cljcrawler.util])
  (:use [cljcrawler.net])
  (:use [cljcrawler.config])
  (:use [cljcrawler.data])
  (:use [clojure.set :only (union)])
  (:use [clojure.string :only (join)])
  (:import [java.io File])
  (:import [java.io ByteArrayInputStream])
  (:import [java.io FileOutputStream])
  (:import [java.nio.file Files])
  (:import [org.apache.commons.io IOUtils]))

(def max-depth (promise))
(def downloader-working (atom false))

(defn convert-char
  [c]
  (let [i (int c) a (int \a) z (int \z) A (int \A) Z (int \Z) i0 (int \0) i9 (int \9)]
  (cond
    (and (>= i a) (<= i z)) c
    (and (>= i A) (<= i Z)) c
    (and (>= i i0) (<= i i9)) c
    (= i (int \_)) c
    (= i (int \.)) c
    (= i (int \:)) \-
    (= i (int \?)) \.
    (= i (int \=)) \=
    (= i (int \/)) \_
    (= i (int \&)) \-
    (= i (int \%)) \-
    :default nil)))

(defn url-to-filename
  [url]
  (cond
    (= url @start-url) "index.html"
    (some #(.endsWith url (str "." %)) #{"js", "css", "gif", "png", "htm", "html", "jpg", "ppt", "pptx", "xls", "xlsx", "doc", "docx", "zip", "pdf"}) (apply str (map convert-char url))
    :default (gen-uuid)))

(defn contains-substring?
  [s sub]
  (>= (.indexOf s sub) 0))


(defn is-big-file [url]
  (some #(.endsWith url (str "." %)) #{"xls", "xlsx", "ppt", "pptx", "doc", "docx", "zip", "pdf"}))

;; flag = downloaded/analyzed/localized/error
(defn download
  [url]
  (try 
    (let [resp (get-url @http-client @local-context url)
          content-type (.getValue (.getFirstHeader resp "Content-Type"))
          status-code (.getStatusCode (.getStatusLine resp))
          filename (url-to-filename url)
          fullpath (str conf-home-dir filename)
          outfile (FileOutputStream. fullpath)]
      (println "starting to download" url)
      (IOUtils/copy 
        (.getContent (.getEntity resp)) 
        outfile)
      (.close resp)
      (.close outfile)
      (println "finish downloading" url)
      {:status-code status-code
        :content-type content-type
        :flag "downloaded"
        :filename filename})
    (catch Exception e 
      (println "*** ERROR while downloading" url "***" (.getMessage e))
      {:flag "error"
       :error (str "ERROR while downloading " url " *** " (.getMessage e))})))

(defn downloader []
  (while (not @stop-phase1-sign) 
    (let [job (pop-url-q) {:keys [url depth]} job]
      (when job
        (reset! downloader-working true)
        (let [off-line-result (download url) {:keys [status-code content-type filename]} off-line-result]
          (add-downloaded url (assoc off-line-result :depth depth)) 
          (when 
            (and
              (not (some #(= status-code %) #{400 404 403}))
              (not (nil? content-type))
              (not (nil? filename))
              (.startsWith content-type "text/html"))
            (if (< depth @max-depth)
              (add-to-analysis-q url filename depth)
              (add-to-localization-q url filename)))))
      (do
        (reset! downloader-working false)
        (Thread/sleep 1000)
        (println "URL-Q: " (count @url-q))))))

(defn kick-off-downloader []
  (.start (Thread. downloader)))