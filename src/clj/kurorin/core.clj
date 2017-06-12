(ns kurorin.core
  (:require [kurorin.github :refer :all]
            [kurorin.publisher :refer :all]
            [bidi.bidi]
            [bidi.ring :refer [make-handler files redirect]]
            [ring.util.response :refer [content-type file-response]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [taoensso.timbre :refer [spy debug get-env]]))

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
  {:filename "test"
     :title "Test Book"
     :author "GitHub users"
     :description "Some great readmes from GitHub."
     :chapters (map mk-chapter (range (count repos)) repos)})

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
