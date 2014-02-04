(defproject CljCrawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [org.jsoup/jsoup "1.7.2"]
                 #_[clj-http/clj-http "0.7.8"]
                 [org.apache.httpcomponents/httpclient "4.3.1"]
                 [commons-io/commons-io "2.4"]
                ]
  :main cljcrawler.cmdmain
  :jvm-opts ["-Xmx1325m" "-server" "-Xss250m"] )
