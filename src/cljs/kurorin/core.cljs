(ns kurorin.core
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [reagent.core :refer [render]]
            [re-frame.core :as r]
            [dommy.core :as dom]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(debug "Starting...")

(def routes
  ["/" {"books" :books        ;; Show list of books
        "compose" :compose}]) ;; Compose a book

(defn- dispatch-route
  "Called when the url matches a route"
  [matched]
  (let [page (keyword (str (name (:handler matched)) "-page"))]
    (debug page)
    (r/dispatch [:move-to-page page])))

(defn app-routes
  []
  (->>
    (partial bidi/match-route routes)
    (pushy/pushy dispatch-route)  ;; Create a pushy instance
    pushy/start!))                ;; And register it

(r/reg-event-db
  :initialize-db
  (fn  [_ _]
    {:current-page :books-page}))

(r/reg-event-db
  :move-to-page
  (fn [db [_ new-page]]
    (assoc db :current-page new-page)))

(r/reg-sub
  :current-page
  (fn [db _]
    (:current-page db)))

(defn- books-page
  []
  (fn []
    [:div
     [:h1 "Book list"]
     [:hr]
     [:a {:href (bidi/path-for routes :compose)} "Compose a new book"]]))

(defn- compose-page
  []
  (fn []
    [:div
     [:h1 "Compose Book"]
     [:hr]
     [:a {:href (bidi/path-for routes :books)} "Back to the book list"]]))

(defn- current-page
  []
  (let [curpage (r/subscribe [:current-page])]
    (fn []
      (case @curpage
        :books-page [books-page]
        :compose-page [compose-page]
        [:div "Unexpected page"]))))

(defn ui
  []
  [:div
   [current-page]])

(defn mount-root []
  (r/clear-subscription-cache!)
  (render [ui]
          (dom/sel1 :#content)))

(defn init
  []
  (app-routes)
  (r/dispatch-sync [:initialize-db])
  (mount-root))

