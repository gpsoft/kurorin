(ns kurorin.publisher
  (:require [kurorin.utils :refer :all]
            [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :refer [spy debug get-env]]))

(def ^{:private true} cfg (load-config))

;; Templates for kindle
(html/deftemplate
  chapter "template/chapter.html"
  [content]
  [:#content] (html/html-content content))

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
  (let [filepath (str dirpath to)]
    (io/make-parents filepath)
    (spit-bin filepath (fetch-bin from))))

(defn publish
  [book]
  (let [tmpdir (System/getProperty "java.io.tmpdir")
        content (get-in book [:chapters 0 :content])
        filepath (str tmpdir "chap1.html")
        imgs (get-in book [:chapters 0 :images])]
    (doall (map (partial download-image tmpdir) imgs))
    (spit filepath (apply str (chapter content)))
    filepath))

(comment
  )
