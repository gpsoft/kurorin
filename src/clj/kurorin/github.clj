(ns kurorin.github
  (:require [kurorin.utils :refer :all]
            [clojure.string :as string]
            [cprop.core :refer [load-config]]
            [clj-http.client :as http]
            [cheshire.core :refer [parse-string]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :refer [spy debug get-env]])
  (:import java.net.URL))

(def cfg (load-config))
(def api-url-base "https://api.github.com")
(def site-url-base "https://github.com")
(def opt-base {:basic-auth (:github-credential cfg)})

;; Simple fetch functions
(defn- fetch
  [url opt json?]
  (let [body (:body (spy (http/get url opt)))]
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
  ([url] (fetch-bin url {}))
  ([url opt]
   (fetch url (assoc opt :as :byte-array) false)))


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
  [repo-name]
  (let [ep (str api-url-base "/repos/" repo-name "/readme")]
    (fetch-str ep (assoc opt-base
                     :headers
                     {:Accept "application/vnd.github.v3.html+json"}))))


;; Manipulate Content and collect info of images
(defn- attr1
  [node attr]
  (first (html/attr-values node attr)))

(defn- img-src
  "画像のurlを得る。
  単純にsrc属性そのままではダメ。src属性のパターンから判断する。
  - https://camo.githubusercontent.com/....
  - 上記以外のFQURL
  - hoge.png
  - /img/hoge.png
  "
  [match repo-m]
  (let [src (attr1 match :src)
        src-alt (attr1 match :data-canonical-src)
        repo-name (:full_name repo-m)
        branch (:default_branch repo-m)]
    (if (re-matches #"^https?://.*$" src)
      (if (string/includes? src "camo.githubusercontent.com") src-alt src)
      (let [src (if (string/starts-with? src "/") src (str "/" src))]
        (str site-url-base "/" repo-name "/raw/" branch src)))))

(defn- path-ext
  "URLから拡張子を得る。
  簡易版。長すぎるのは無視。"
  [url-str]
  (let [path (.getPath (URL. url-str))
        ext (last (string/split path #"\."))]
    (if (> (count ext) 4) "" (str "." ext))))

(defn- mkfn:img-handler
  "imgタグを改ざんするハンドラを生成。"
  [repo-m img-prefix]
  (let [img-name (atom 0)]
    (fn [match]
      (swap! img-name inc)
      (let [org-src (img-src match repo-m)
            ext (path-ext org-src)
            new-src (str img-prefix @img-name ext)]
        #_(spit-bin new-src (fetch-bin org-src))
        (-> match
            ((html/set-attr :src new-src))
            ((html/set-attr :data-org-src org-src)))))))

(defn manipulate-content
  "ドキュメントを改ざん。
  - 見出しアンカーsvgを非表示
  - imgタグのsrc変更とdata-org-src追加"
  [doc repo-m img-prefix]
  (let [dom (html/html-snippet doc)
        _ (html/defsnippet snip dom [:article] []
                           [:a.anchor :svg] (html/set-attr :style "display:none")
                           [:img] (mkfn:img-handler repo-m img-prefix))]
    (apply str (html/emit* (snip)))))

(defn link-images
  "ドキュメント内の画像リストを得る。"
  [doc]
  (letfn [(info [img]
            [(attr1 img :data-org-src)
             (attr1 img :src)])]
    (-> (html/html-snippet doc)
      (html/select [:img])
      (->> (map info)))))

(comment
  (search-user "gpsoft")
  (search-repo "neko")
  (let [repo-m {:full_name "Day8/re-frame"
                :default_branch "master"
                :login "Day8"
                :name "re-frame"}
        doc fuga #_(fetch-readme (:full_name repo-m))
        doc (manipulate-content doc repo-m "img/hoge/")]
    (link-images doc))
  (spit-bin "/tmp/hoge.png" (fetch-bin (str site-url-base "/gpsoft/tv-girl/raw/master/ss1.png") ))
  (.getPath (URL. "http://hoge/fuga/piyo.png?a=1"))
  )
