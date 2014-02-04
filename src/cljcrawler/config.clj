(ns cljcrawler.config)

;; directory to store downloaded files, be sure to ends with a /
(def conf-home-dir "/home/alfred/xiaoyf/test/alserver1/")

;; only those URLs that starts with below prefixes are fetched/crawled
(def allowed-prefix #{"http://alserver1.cn.oracle.com/autoSR2/"
                      })

;; which URL to start working on
(def conf-start-url "http://alserver1.cn.oracle.com/autoSR2/index.html")

;; added as prefix to those href starting with /
(def conf-site-base "http://alserver1.cn.oracle.com")