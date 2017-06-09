(ns kurorin.github
  (:require [kurorin.utils :refer :all]
            [cprop.core :refer [load-config]]
            [clj-http.client :as http]
            [cheshire.core :refer [parse-string]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :refer [spy debug get-env]]))

(def cfg (load-config))
(def api-url-base "https://api.github.com")
(def site-url-base "https://github.com")
(def opt-base {:basic-auth (:github-credential cfg)})

;; Simple fetch functions
(defn- fetch
  [url opt json?]
  (debug (get-env))
  (let [body (:body (http/get url opt))]
    (if json?
      (parse-string body true)   ;; true: keywordised key
      body)))
(defn- fetch-json
  [url opt]
  (fetch url opt true))
(defn- fetch-str
  [url opt]
  (fetch url opt false))
(defn- fetch-bin
  [url opt]
  (fetch url (assoc opt :as :byte-array) false))


;; Using GitHub API
(defn search-user
  "Search users. Result is in JSON."
  [kw]
  (let [ep (str api-url-base "/search/users?q=" kw)]
    (fetch-json ep opt-base)))

(defn search-repo
  "Search repositories. Result is in JSON."
  [kw]
  (let [ep (str api-url-base "/search/repositories?q=" kw)]
    (fetch-json ep opt-base)))

(defn fetch-readme
  "Fetch readme of the repo. Result is in partial HTML string."
  [user repo]
  (let [ep (str api-url-base "/repos/" user "/" repo "/readme")]
    (fetch-str ep (assoc opt-base
                     :headers
                     {:Accept "application/vnd.github.v3.html+json"}))))

;; Manipulate Content and collect images
(defn- img-src
  [match user repo]
  (let [src (first (html/attr-values match :src))
        src-alt (first (html/attr-values match :data-canonical-src))]
    (if (re-matches #"^https?://.*$" src) src-alt
      (str site-url-base "/" user "/" repo "/raw/master/" src))))

(defn hide-anchor-svg
  [doc user repo]
  (let [img-handler (fn [match]
                      (html/attr-values match :src)
                      ((html/set-attr :src (img-src match user repo)) match)
                      )
        dom (html/html-snippet doc)
        _ (html/defsnippet hoge dom [:article] []
                           [:a.anchor :svg] (html/set-attr :style "display:none")
                           [:img] img-handler)
        ]
    (apply str (html/emit* (hoge)))))

(comment
  (search-user "gpsoft")
  (search-repo "neko")
  (fetch-readme "cgrand" "enlive")
  (let [user "gpsoft"
        repo "tv-girl"
        doc (fetch-readme user repo)]
    (hide-anchor-svg doc user repo))
  (spit-bin "/tmp/hoge.png" (fetch-bin (str site-url-base "/gpsoft/tv-girl/raw/master/ss1.png") {}))
  )
