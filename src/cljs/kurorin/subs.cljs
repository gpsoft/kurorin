(ns kurorin.subs
  (:require [re-frame.core :as r]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(r/reg-sub
  :search-result
  (fn [db query-v]
    (:search-result db)))

(r/reg-sub
  :chapters
  (fn [db query-v]
    (:chapters db)))
