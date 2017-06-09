(ns kurorin.utils
  (:require [clojure.java.io :refer [file output-stream]]))

(defn spit-bin
  "Spit binary data to a file."
  [filepath data]
  (with-open [out (output-stream (file filepath))]
    (.write out data)))
