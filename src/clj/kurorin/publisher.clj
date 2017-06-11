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


;; Book map
;;   :filename string
;;   :title string
;;   :author string
;;   :chapters [Chapter]
;; Chapter map
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

(defn publish
  [book]
  (let [workdir (str (tmp-dir) (:filename book) "/")
        content (get-in book [:chapters 0 :content])
        imgs (get-in book [:chapters 0 :images])]
    (doall (map (partial download-image workdir) imgs))
    (spit-on-dir workdir "index.html" (apply str (index book)))
    (spit-on-dir workdir "chap1.html" (apply str (chapter content)))
    workdir))

(comment
  )
