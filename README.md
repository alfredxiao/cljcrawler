# CljCrawler

A crawler in Clojure which allows you to download a website, or a subset of it, for offline viewing.

## Usage
1. Presently, you need to set application parameters in config.clj before running
2. To run it

        lein run MAX_DEPTH
        ;; MAX_DEPTH determins how deep down the web, which can be considered a tree with starting URL as the root, you are going to crawl/fetch.

## TODO
1. To include more types of (links) in an HTML, like CSS imports
2. Add authentication handling (BASIC/Form/...)
3. Allows a more flexible way of setting parameters like start url, etc.
4. Parallelize file downloading

## License

Copyright © 2014

Distributed under the Eclipse Public License, the same as Clojure.
