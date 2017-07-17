(ns kurorin.github
  (:require [kurorin.utils :refer :all]
            [clojure.string :as string]
            [cprop.core :refer [load-config]]
            [net.cgrand.enlive-html :as html]
            [me.raynes.fs :as fs]
            [taoensso.timbre :refer [spy debug get-env]])
  (:import java.net.URL))

(def ^{:private true} cfg (load-config))
(def api-url-base "https://api.github.com")
(def site-url-base "https://github.com")
(def opt-base {:basic-auth (:github-credential cfg)})


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

(defn list-dir-contents
  "List contents of a directory."
  [repo-name dir-path]
  (let [ep (str api-url-base "/repos/" repo-name "/contents" dir-path)]
    (fetch-json ep
                opt-base)))

(defn fetch-content
  "Fetch a file readme of the repo. Result is in partial HTML string."
  [repo-name file-path]
  (let [ep (str api-url-base "/repos/" repo-name "/contents" file-path)]
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

(defn- markup?
  [content]
  (let [exts #{".md" ".markdown"
               ".asciidoc" ".asc"
               ".org" ".rdoc"
               ".textile"}]
    (and (= (:type content) "file")
         (exts (fs/extension (:path content))))))

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

(defn- mkfn:snippet
  [repo-m img-prefix disable-anchor? dom]
  (html/defsnippet
    readme dom [:article]
    []
    [:a.anchor] (html/do->
                  (html/content "")
                  html/unwrap)
    [[:a (html/attr= :target "_blank") (html/has [:img])]] html/unwrap
    [:a] (if disable-anchor?
           (html/do-> html/unwrap (html/wrap :span {:class "anchor"}))
           identity)
    [:img] (mkfn:img-handler repo-m img-prefix)))

(defn manipulate-content
  "ドキュメントを改ざん。
  - 見出しアンカーsvgを非表示
  - 別タブに開くアンカーを無効化
  - imgタグのsrc変更とdata-org-src追加"
  [doc repo-m img-prefix disable-anchor?]
  (let [dom (html/html-snippet disable-anchor? doc)
        _ (mkfn:snippet repo-m img-prefix disable-anchor? dom)]
    (apply str (html/emit* (readme)))))

(defn link-images
  "ドキュメント内の画像リストを得る。"
  [doc]
  (letfn [(info [img]
            {:from (attr1 img :data-org-src)
             :to (attr1 img :src)})]
    (-> (html/html-snippet doc)
      (html/select [:img])
      (->> (map info)))))

(comment
  (search-user "gpsoft")
  (search-repo "neko")
  (fetch-readme "gpsoft/kurorin")
  (->>
    (list-dir-contents "exercism/clojure" "/docs")
    (filter markup?)
    (map :path))
  (fetch-content "Day8/re-frame" "/docs/Coeffects.md")
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
