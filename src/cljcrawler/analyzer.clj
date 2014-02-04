(ns cljcrawler.analyzer
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

(def analyzer-working (atom false))

(defn should-visit?
  [url]
  (when url
    (and 
      (not (nil? url))
      (or (some #(.startsWith url %) allowed-prefix))
      (< (.indexOf url "/../") 0)
      (not (.startsWith url "mailto:"))
      (not (.startsWith url "javascript:")))))

(defn analyze
  [filename ref-url]
  (let [doc (Jsoup/parse (slurp (str conf-home-dir filename)))]
    (filter #(not (nil? %))
      (map #(make-complete-url % ref-url) 
         (union (set (map #(.attr % "href") (.select doc "a")))
               (set (map #(.attr % "href") (.select doc "area")))
               (set (map #(.attr % "src") (.select doc "script")))
               (set (map #(.attr % "src") (.select doc "img")))
               (set (map #(.attr % "href") (.select doc "link"))))))))


(defn analyzer []
  (while (not @stop-phase1-sign) 
    (let [job (pop-analysis-q) {:keys [url filename depth]} job]
      (when job
        (reset! analyzer-working true)
        (let [out-links (analyze filename url)]
          (update-process-links url out-links)
          (add-to-localization-q url filename)
          (doseq [out-link out-links] 
            (if (and (not (contains? @process-data out-link)) (should-visit? out-link))
              (add-to-url-q out-link (inc depth))
              (println "IGNORING out-link" out-link))
            (update-process-flag url "analyzed"))))
      (do 
        (reset! analyzer-working false)
        (Thread/sleep 3000)
        (println "analysis-Q: " (count @analysis-q))))))

(defn kick-off-analyzer []
  (.start (Thread. analyzer)))
  