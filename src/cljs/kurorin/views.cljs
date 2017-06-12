(ns kurorin.views
  (:require [kurorin.routes :refer [rev-route]]
            [kurorin.subs]
            [reagent.core :as reagent]
            [re-frame.core :as r]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(defn- search-box
  [kw]
  (let [val (reagent/atom kw)
        search #(let [v (-> @val str clojure.string/trim)]
                  (when-not (empty? v) (r/dispatch [:search-github v])))]
    (fn []
      [:div
       [:div.form-inline.text-center
        [:div.form-group
         [:div.input-group
          [:div.input-group-addon
           [:span.glyphicon.glyphicon-search]]
          [:input.form-control {:id "kw"
                                :type "text"
                                :placeholder "Keyword for users or repos"
                                :value @val
                                :on-change #(reset! val (-> % .-target .-value))
                                :on-key-down #(when (= (.-which %) 13) (search))}]]]
        [:button.btn.btn-primary.ml8 {:on-click #(search)} "Search"]]])))

(defn- search-result
  []
  (let [result (r/subscribe [:search-result])]
    (fn []
      [:ul
       (for [item (:items @result)]
         ^{:key item} [:li
                       {:on-click #(r/dispatch [:append-chapter item])}
                       [:div (:full_name item)]
                       [:div (:description item)]])])))

(defn- chapter-list
  []
  (let [chapters (r/subscribe [:chapters])]
    (fn []
      [:ul
       (for [chap @chapters]
         ^{:key chap} [:li
                       (:full_name chap)])])))

(defn- compose-panel
  []
  (let [chapters (r/subscribe [:chapters])]
    (fn []
      (when (seq @chapters)
        [:button.btn.btn-success {:on-click #(r/dispatch [:publish])} "Publish"]))))

(defn- books-page
  []
  (fn []
    [:div
     [:h1 "Book list"]
     [:hr]
     [:a {:href (rev-route :compose)} "Compose a new book"]]))

(defn- compose-page
  []
  (fn []
    [:div
     [:h1 "Compose Book"]
     [search-box ""]
     [:div.row
      [:div.col-sm-5
       [search-result]]
      [:div.col-sm-1]
      [:div.col-sm-6
       [chapter-list]
       [compose-panel]]]
     [:a {:href (rev-route :books)} "Back to the book list"]]))

(defn- current-page
  []
  (let [curpage (r/subscribe [:current-page])]
    (fn []
      (case @curpage
        :books-page [books-page]
        :compose-page [compose-page]
        [:div "Unexpected page"]))))

(defn main-ui
  []
  [:div
   [current-page]])
