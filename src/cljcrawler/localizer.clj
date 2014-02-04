(ns cljcrawler.localizer
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
  (:import [org.jsoup Jsoup])
  (:import [org.apache.commons.io IOUtils]))

(def localizer-working (atom false))

(defn get-filename-from-url
  [url]
  (:filename (get @process-data url))) 

(defn localize-general-link
  [doc ref selector attr-name]
  (doseq [e (.select doc selector)]
    (let [old-value (.attr e attr-name)
          new-value (get-filename-from-url (make-complete-url old-value ref))] 
      (.attr e attr-name 
        (if new-value new-value old-value)))))

(defn localize-links
  [doc ref]
  (localize-general-link doc ref "a" "href")
  (localize-general-link doc ref "area" "href")
  (localize-general-link doc ref "script" "src")
  (localize-general-link doc ref "img" "src")
  (localize-general-link doc ref "link" "href")
  (.html doc))

(defn localize
  [filename url]
  (let [fullpath (str conf-home-dir filename) fullpath-orig (str fullpath ".orig")]
    (println "localizing file fullpath=" fullpath)
    (.renameTo (File. fullpath) (File. fullpath-orig))
    (spit fullpath (localize-links (Jsoup/parse (slurp fullpath-orig)) url))
    (println "finished localizing file fullpath=" fullpath)))

(defn localizer []
  (while (not @stop-phase2-sign) 
    (let [{:keys [url filename]} (pop-localization-q)]
      (when filename
        (reset! localizer-working true)
        (localize filename url)
        (update-process-flag url "localized"))
      (do 
        (reset! localizer-working false)
        (Thread/sleep 3000)
        (println "localization-Q:" (count @localization-q))))))

(defn kick-off-localizer []
  (.start (Thread. localizer)))