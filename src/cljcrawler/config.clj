(ns cljcrawler.config)

;; directory to store downloaded files, be sure to ends with a /
(def conf-home-dir "/home/alfred/xiaoyf/test/vicroads/")

;; only those URLs that starts with below prefixes are fetched/crawled
(def allowed-prefix #{"http://www.vicroads.vic.gov.au/Home/SafetyAndRules/"
                      "http://www.vicroads.vic.gov.au"})

;; which URL to start working on
(def conf-start-url "http://www.vicroads.vic.gov.au/Home/SafetyAndRules/")

;; added as prefix to those href values starting with /
(def conf-site-base "http://www.vicroads.vic.gov.au")
