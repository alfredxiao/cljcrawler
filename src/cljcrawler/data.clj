(ns cljcrawler.data)

(def url-q (atom []))
(def analysis-q (atom []))
(def localization-q (atom []))
(def process-data (atom {}))
(def stop-phase1-sign (atom false))
(def stop-phase2-sign (atom false))

(defn- pop-q
  [q]
  (let [top (peek @q)]
    (when top (swap! q pop)
      top)))

(defn- add-to-q
  [q item]
  (swap! q conj item))

(defn pop-url-q [] (pop-q url-q))
(defn add-to-url-q [url depth]
  (println "ADDING to url-q" url)
  (add-to-q url-q {:url url :depth depth}))

(defn pop-analysis-q[] (pop-q analysis-q))
(defn add-to-analysis-q [url filename depth] 
  (println "ADDING to analysis-q" url filename)
  (add-to-q analysis-q {:url url :filename filename :depth depth}))

(defn pop-localization-q [] (pop-q localization-q))
(defn add-to-localization-q [url filename] 
  (println "ADDING to localization-q" url filename)
  (add-to-q localization-q {:url url :filename filename}))

(defn add-downloaded
  [url meta-map]
  (swap! process-data assoc url meta-map))

(defn change-key
 [m old-k new-k]
 (let [v (get m old-k)]
   (assoc (dissoc m old-k) new-k v)))

(defn update-process-key
  [old-url new-url]
  (change-key process-data old-url new-url))

(defn update-process-flag
  [url new-flag]
  (swap! process-data assoc-in [url :flag] new-flag))

(defn update-process-links
  [url links]
  (swap! process-data assoc-in [url :links] links))
