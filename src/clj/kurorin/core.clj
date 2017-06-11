(ns kurorin.core
  (:require [kurorin.github :refer :all]
            [kurorin.publisher :refer :all]
            [bidi.bidi]
            [bidi.ring :refer [make-handler files redirect]]
            [ring.util.response :refer [content-type file-response]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [taoensso.timbre :refer [spy debug get-env]]))

(declare fuga)
(defn mk-book
  [repos]
  (let [repo-m {:full_name "Day8/re-frame"
                :default_branch "master"
                :login "Day8"
                :name "re-frame"}
        doc (fetch-readme (:full_name repo-m))
        doc (manipulate-content doc repo-m "img/hoge/")
        imgs (link-images doc)]
    {:filename "test"
     :title "Test Book"
     :author "GitHub users"
     :description "Some great readmes from GitHub."
     :chapters
     [{:no 1
       :caption "chap1"
       :content doc
       :images imgs}]}))

(def api-routes
  ["/api/" {"hoge" :hoge
           "fuga" :fuga}])
(def site-routes
  ["/" {"" (redirect "index.html")
        #{"books" "compose"} (fn [req] (-> (file-response "index.html" {:root "target"})
                              (content-type "text/html")))
        "index.html" (files {:dir "target"})
        "js/" (files {:dir "target/js"})
        "css/" (files {:dir "target/css"})}])
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


(comment
  (publish (mk-book nil))

  )
