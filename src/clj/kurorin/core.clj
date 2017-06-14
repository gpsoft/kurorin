(ns kurorin.core
  (:require [kurorin.github :refer :all]
            [kurorin.publisher :refer :all]
            [bidi.bidi]
            [bidi.ring :refer [make-handler resources redirect]]
            [ring.util.response :refer [content-type resource-response]]
            [ring.util.json-response :refer [json-response]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.adapter.jetty :as jetty]
            [taoensso.timbre :refer [spy debug get-env]])
  (:gen-class))

(defn- mk-chapter
  [ix repo-m]
  (let [no (inc ix)
        repo-name (:full_name repo-m)
        doc (fetch-readme repo-name)
        doc (manipulate-content doc repo-m (str "img/" no "/") true)
        imgs (link-images doc)]
    {:no no
     :caption repo-name
     :content doc
     :images imgs}))

(defn- mk-book
  [repos]
  {:filename "book1"
     :title "Awesome readmes from GitHub"
     :author "GitHub users"
     :description "Some great readmes from GitHub."
     :chapters (map mk-chapter (range (count repos)) repos)})

(defn- api-publish
  [req]
  (publish! (mk-book (:body req)))
  (json-response {:result "OK"}))

(def api-routes
  ["/api/" {"publish" api-publish
           "fuga" :fuga}])
(def site-routes
  ["/" {"" (redirect "books")
        #{"books" "compose"}
        (fn [req] (-> (resource-response "index.html")
                      (content-type "text/html")))
        "js/" (resources {:prefix "js/"})
        "css/" (resources {:prefix "css/"})}])
(def app
  (let [api-h (-> (make-handler api-routes)
                  (wrap-json-body {:keywords? true})
                  wrap-json-response
                  (wrap-defaults api-defaults))
        site-h (-> (make-handler site-routes)
                   (wrap-defaults site-defaults))]
    (fn [req]
      (if-let [res (api-h req)]
        res
        (site-h req)))))


(defn -main []
  (jetty/run-jetty app {:port 3333}))

(comment
  (publish! (mk-book [{:full_name "gpsoft/tv-girl"
                       :default_branch "master"
                       :login "gpsoft"
                       :name "tv-girl"}
                      {:full_name "gpsoft/othe"
                       :default_branch "master"
                       :login "gpsoft"
                       :name "othe"}
                      #_{:full_name "Day8/re-frame"
                       :default_branch "master"
                       :login "Day8"
                       :name "re-frame"}]))
  )
