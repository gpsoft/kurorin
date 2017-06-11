(ns kurorin.utils
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string]]
            [clojure.java.io :refer [file output-stream]]))

(defn spit-bin
  "Spit binary data to a file."
  [filepath data]
  (with-open [out (output-stream (file filepath))]
    (.write out data)))

;; Simple fetch functions
(defn- fetch
  [url opt json?]
  (let [body (:body (http/get url opt))]
    (if json?
      (parse-string body true)   ;; true: keywordised key
      body)))
(defn fetch-json
  [url opt]
  (fetch url opt true))
(defn fetch-str
  [url opt]
  (fetch url opt false))
(defn fetch-bin
  ([url] (fetch-bin url {}))
  ([url opt]
   (fetch url (assoc opt :as :byte-array) false)))
