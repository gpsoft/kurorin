(ns kurorin.views
  (:require [kurorin.routes :refer [rev-route]]
            [kurorin.subs]
            [reagent.core :as reagent]
            [re-frame.core :as r]
            [dommy.core :as dom]
            [cljsjs.jquery-ui]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(defn- search-box
  [kw]
  (let [ajax? (r/subscribe [:on-ajax?])
        val (reagent/atom kw)
        search #(let [v (-> @val str clojure.string/trim)]
                  (when-not (empty? v) (r/dispatch [:search-github v])))]
    (fn []
      [:div.mb16
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
        [:button.btn.btn-primary.ml8 {:on-click #(search)} "Search"]
        [:label.on-ajax
         (when @ajax? [:span.spinner])]]])))

(defn- repo-result
  [repo-item]
  (let [avatar (get-in repo-item [:owner :avatar_url] "")
        html-url (get-in repo-item [:html_url])]
    [:li.repo-item
     {:on-click #(when (not= (.-target.tagName %) "SPAN")
                   (r/dispatch [:append-chapter repo-item]))}
     [:div.table-row
      [:div.table-cell.avatar
       (when-not (empty? avatar) [:img {:src avatar}])]
      [:div.table-cell.repo-info
       [:div.repo-name
        (:full_name repo-item)
        [:a.repo-link {:href html-url :target "_blank"}
         [:span.glyphicon.glyphicon-new-window]]]
       [:div.repo-description (:description repo-item)]]]]))

(defn- search-result
  []
  (let [result (r/subscribe [:search-result])]
    (fn []
      (let [items (:items @result)]
        (if (seq items)
          [:ul
           (for [item items]
             ^{:key item} [repo-result item])]
          [:p "No match."])))))

(defn chap-to-attr
  [{:keys [full_name default_branch login name]}]
  {:data-full-name full_name
   :data-default_branch default_branch
   :data-login login
   :data-name name})

(defn chap-from-attr
  [ele]
  {:full_name (dom/attr ele "data-full-name")
   :default_branch (dom/attr ele "data-default_branch")
   :login (dom/attr ele "data-login")
   :name (dom/attr ele "data-name")})

(defn- chapter
  [{:keys [full_name default_branch] :as chap}]
  [:li.chapter (chap-to-attr chap)
   [:div.table-row
    [:div.table-cell.chapter-info
     [:span.repo-name full_name]
     [:span (str " (" default_branch ")")]]
    [:div.table-cell.chapter-btns
     [:a.btn-del-chapter
      {:on-click #(r/dispatch [:remove-chapter full_name])}
      [:span.glyphicon.glyphicon-remove]]
     ]]])

(defn- chapter-list
  [chapters]
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (-> this
           reagent/dom-node
           js/$
           (.sortable #js {:axis "y"
                           :cursor "move"
                           :scroll true
                           :items "li"
                           :handle ".chapter-info"
                           :stop #(r/dispatch [:chapter-sorted])})))
     :reagent-render
     (fn [chapters]
       (fn []
         [:ul
          (for [chap @chapters]
            ^{:key chap} [chapter chap])]))}))

(defn- book-info
  []
  (let [chapters (r/subscribe [:chapters])]
    (fn []
      (if (seq @chapters)
        [chapter-list chapters]
        [:p "Search and select repo to add chapters."]))))

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
       [:h4 "Search Result"]
       [search-result]]
      [:div.col-sm-1]
      [:div.col-sm-6
       [:h4 "Chapters to publish"]
       [book-info]
       [compose-panel]]]
     [:div.footer
      [:a.back-link {:href (rev-route :books)} "Back to the book list"]]]))

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
