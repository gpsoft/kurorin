(ns kurorin.utils
  (:require [clj-http.client :as http]
            [cheshire.core :refer [parse-string]]
            [clojure.string :as string]
            [clojure.java.io :refer [file output-stream make-parents]]))

;; File IO
(defn spit-on-dir
  "Spit to a file on a dir."
  [dirpath filename data]
  (let [filepath (str dirpath filename)]
    (make-parents filepath)
    (spit filepath data)))
(defn spit-bin
  "Spit binary data to a file."
  [filepath data]
  (make-parents filepath)
  (with-open [out (output-stream (file filepath))]
    (.write out data)))
(defn tmp-dir
  "Temp directory (ends with /)"
  []
  (let [d (System/getProperty "java.io.tmpdir")]
    (if (string/ends-with? d "/") d (str d "/"))))

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
