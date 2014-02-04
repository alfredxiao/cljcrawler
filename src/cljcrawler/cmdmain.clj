(ns cljcrawler.cmdmain
  (:use [cljcrawler.config])
  (:use [cljcrawler.data])
  (:use [cljcrawler.net])
  (:use [cljcrawler.downloader])
  (:use [cljcrawler.analyzer])
  (:use [cljcrawler.localizer])
  (:gen-class)
)


(defn count-phase1-q []
  (+ (count @url-q)
     (count @analysis-q)))

(defn count-phase2-q []
  (+ (count @localization-q)))

(defn boolean-to-int [bool] (if bool 1 0))

(defn count-phase1-working []
  (+ (boolean-to-int @downloader-working)
     (boolean-to-int  @analyzer-working)))

(defn count-phase2-working []
  (+ (boolean-to-int @localizer-working)))

(defn -main
   [& args]
   
   (deliver max-depth (Integer/parseInt (first args)))
   (kick-off-downloader)
   (println "downloader kicked off")

   (kick-off-analyzer)
   (println "analyzer kicked off")

   (println "preparing connection")
   (prepare-http-conn)
   (println "connection ready")
   
   (println "adding start-url to job queue")
   (add-to-url-q @start-url 0)
   
   (Thread/sleep 5000)
   (while 
     (or (> (count-phase1-q) 0) 
         (> (count-phase1-working) 0))
     (Thread/sleep 2000))

   (reset! stop-phase1-sign true)

   (kick-off-localizer)
   (println "localizer kicked off")

   (while 
     (or (> (count-phase2-q) 0) 
         (> (count-phase2-working) 0))
     (Thread/sleep 2000))

   (reset! stop-phase2-sign true)
   (Thread/sleep 2000)
   (spit "process.data" @process-data) 
)