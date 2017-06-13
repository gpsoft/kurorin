(ns kurorin.events
  (:require [kurorin.views]
            [re-frame.core :as r]
            [ajax.core :refer [GET POST]]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(declare last-result)
(r/reg-event-db
  :initialize-db
  (fn  [_ _]
    {:current-page :books-page
     ; :search-result last-result   ;; for debugging
     :chapters
     []
     #_[{:full_name "clojure/clojure", :default_branch "master", :login "clojure", :name "clojure"}
      {:full_name "LightTable/Clojure", :default_branch "master", :login "LightTable", :name "Clojure"}
      {:full_name "zcaudate/hara", :default_branch "master", :login "zcaudate", :name "hara"}]    ;; for debugging
     :on-ajax? false}))

(r/reg-event-db
  :move-to-page
  (fn [db [_ new-page]]
    (assoc db :current-page new-page)))

(r/reg-event-db
  :search-result
  (fn [db [_ {:keys [total_count items] :as result}]]
    (def last-result result)
    (let [num-items (min 30 total_count)]
      (assoc db
             :search-result {:num-items num-items
                             :total-num-items total_count
                             :items (take num-items items)}
             :on-ajax? false))))

(r/reg-event-db
  :publish-ok
  (fn [db [_]]
    (assoc db :flash "Published!" :on-ajax? false)))

(r/reg-event-db
  :ajax-error
  (fn [db [_ result]]
    (assoc db :flash "Ajax fail" :on-ajax? false)))

(defn- repo=?
  [repo-name repo-m]
  (= (:full_name repo-m) repo-name))

(defn- append-chapter
  [chapters {:keys [full_name default_branch owner] :as item}]
  (let [repo-name (:name item)
        login (:login owner)]
    (when (not-any? (partial repo=? repo-name) chapters)
      (conj chapters {:full_name full_name
                      :default_branch default_branch
                      :login login
                      :name repo-name}))))

(defn- remove-chapter
  [chapters repo-name]
  (filterv (comp not (partial repo=? repo-name)) chapters))

(r/reg-event-db
  :remove-chapter
  (fn [db [_ repo-name]]
    (update-in db [:chapters] remove-chapter repo-name)))

(r/reg-event-db
  :append-chapter
  (fn [db [_ item]]
    (update-in db [:chapters] append-chapter item)))

(r/reg-fx
  :http
  (fn [{:keys [url on-success on-fail] :as req-m}]
    (GET url {:format :json
              :response-format :json
              :keywords? true
              :handler #(r/dispatch (conj on-success %))
              :error-handler #(r/dispatch (conj on-fail %))})))

(r/reg-fx
  :publish-api
  (fn [{:keys [chapters on-success on-fail] :as req-m}]
    (POST "/api/publish"
          {:format :json
           :params chapters
           :response-format :json
           :keywords? true
           :handler #(r/dispatch (conj on-success %))
           :error-handler #(r/dispatch (conj on-fail %))})))

(r/reg-event-fx
  :search-github
  (fn [{:keys [db]} [_ kw]]
    {:http {:url (str "https://api.github.com/search/repositories?q=" kw)
            :on-success [:search-result]
            :on-fail [:ajax-error]}
     :db (assoc db :on-ajax? true)}))

(r/reg-event-fx
  :publish
  (fn [{:keys [db]} _]
    {:publish-api {:chapters (:chapters db)
                   :on-success [:publish-ok]
                   :on-fail [:ajax-error]}
     :db (assoc db :on-ajax? true)}))

