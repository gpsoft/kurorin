(ns kurorin.publisher
  (:require [kurorin.utils :refer :all]
            [clojure.java.shell :refer [sh]]
            [cprop.core :refer [load-config]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :refer [spy debug get-env]]))

(def ^{:private true} cfg (load-config))

;; Templates for kindle
(defn- chap-file
  "チャプタ用のhtmlファイル名"
  [chap-or-no]
  (let [no (if (map? chap-or-no) (:no chap-or-no) chap-or-no)]
    (str "chap" no ".html")))
(html/deftemplate
  chapter "template/chapter.html"
  [content]
  [:#content] (html/html-content content))
(html/defsnippet
  toc-item "template/index.html" [:.toc]
  [chapter]
  [:a] (html/do->
         (html/content (:caption chapter))
         (html/set-attr :href (chap-file chapter))))
(html/deftemplate
  index "template/index.html"
  [{:keys [title author chapters]}]
  [:#title] (html/content title)
  [:#author] (html/content author)
  [:#toc] (html/content (map #(toc-item %) chapters)))
(html/defsnippet
  navpoint "template/toc.ncx" [:#navmap :> html/first-child]
  [{:keys [no caption]}]
  [:navPoint] (html/do->
                (html/set-attr :id (if (zero? no) "index" (str "item" no)))
                (html/set-attr :playOrder no))
  [:text] (html/content caption)
  [:content] (html/set-attr :src (if (zero? no) "index.html" (chap-file no))))
(html/deftemplate
  ncx "template/toc.ncx"
  [{:keys [title chapters]}]
  [:#title] (html/content title)
  [:#navmap] (html/content (map #(navpoint %) (concat [{:no 0 :caption "TOC"}] chapters))))
(html/deftemplate
  opf "template/book.opf"
  [{:keys [title author description chapters]}]
  [:#title] (html/content title)
  [:#author] (html/content author)
  [:#description] (html/content description)
  [:#date] (html/content (.toString (java.time.LocalDate/now)))
  [:manifest :item.item] (html/clone-for
                           [{no :no} chapters]
                           (html/do->
                             (html/set-attr :id (str "item" no))
                             (html/set-attr :href (chap-file no))))
  [:spine :itemref.itemref] (html/clone-for
                              [{no :no} chapters]
                              (html/set-attr :idref (str "item" no)))
  )


;; Book map
;;   :filename string
;;   :title string
;;   :author string
;;   :description string
;;   :chapters [Chapter]
;; Chapter map
;;   :no integer
;;   :caption string
;;   :content string(HTML)
;;   :images [Image]
;; Image map
;;   :from string
;;   :to string

(defn- download-image
  [dirpath {:keys [from to]}]
  (debug from)
  (let [filepath (str dirpath to)]
    (spit-bin filepath (fetch-bin from))))

(defn- render [strs] (apply str strs))

(defn- publish-chapter!
  [workdir {:keys [no caption content images]}]
  (doall (map (partial download-image workdir) images))
  (spit-on-dir workdir (chap-file no) (render (chapter content))))

(defn publish!
  [book]
  (let [artifact (:filename book)
        workdir (str (tmp-dir) "kurorin/" artifact "/")]
    (copy-resource "template/" "jacket.jpg" workdir)
    (copy-resource "template/css/" "kindle.css" (str workdir "css/"))
    (spit-on-dir workdir "index.html" (render (index book)))
    (spit-on-dir workdir "toc.ncx" (render (ncx book)))
    (spit-on-dir workdir (str artifact ".opf") (render (opf book)))
    (doall (map (partial publish-chapter! workdir) (:chapters book)))
    (sh "kindlegen" (str workdir artifact ".opf"))
    workdir))

(comment
  )
