(ns kurorin.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as r]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(def ^{:private true} routes
  ["/" {"books" :books        ;; Show list of books
        "compose" :compose}]) ;; Compose a book

(defn- dispatch-route
  "Called when the url matches a route"
  [matched]
  (let [page (keyword (str (name (:handler matched)) "-page"))]
    (r/dispatch [:move-to-page page])))

(defn app-routes
  []
  (->>
    (partial bidi/match-route routes)
    (pushy/pushy dispatch-route)  ;; Create a pushy instance
    pushy/start!))                ;; And register it

(defn rev-route
  [handler]
  (bidi/path-for routes handler))
