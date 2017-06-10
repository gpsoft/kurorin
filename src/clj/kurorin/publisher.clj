(ns kurorin.publisher
  (:require [cprop.core :refer [load-config]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :refer [spy debug get-env]]))

(def cfg (load-config))

;; Book map
;;   :filename string
;;   :title string
;;   :author string
;;   :chapters [Chapter]
;; Chapter map
;;   :chaption string
;;   :content string(HTML)
;;   :images [Image]
;; Image map
;;   :filename string
;;   :content blob

(defn publish
  [book]
  )

(comment
  )
