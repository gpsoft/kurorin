(ns kurorin.publisher
  (:require [kurorin.utils :refer :all]
            [cprop.core :refer [load-config]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :refer [spy debug get-env]]))

(def ^{:private true} cfg (load-config))

;; Templates for kindle
(html/deftemplate
  chapter "template/chapter.html"
  [content]
  [:#content] (html/html-content content))
(html/defsnippet
  toc-item "template/index.html" [:.toc]
  [chapter]
  [:a] (html/do->
         (html/content (:caption chapter))
         (html/set-attr :href (str "chap" (:no chapter) ".html"))))
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
  [:content] (html/set-attr :src (if (zero? no) "index.html" (str "chap" no ".html"))))
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
                             (html/set-attr :href (str "chap" no ".html"))))
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

(defn publish!
  [book]
  (let [workdir (str (tmp-dir) (:filename book) "/")
        content (get-in book [:chapters 0 :content])
        imgs (get-in book [:chapters 0 :images])]
    #_(doall (map (partial download-image workdir) imgs))
    #_(spit-on-dir workdir "index.html" (apply str (index book)))
    (spit-on-dir workdir "toc.ncx" (apply str (ncx book)))
    (spit-on-dir workdir (str (:filename book) ".opf") (apply str (opf book)))
    #_(spit-on-dir workdir "chap1.html" (apply str (chapter content)))
    workdir))

(comment
  )
