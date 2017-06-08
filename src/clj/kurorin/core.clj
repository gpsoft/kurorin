(ns kurorin.core
  (:require [bidi.bidi]
            [bidi.ring :refer [make-handler files redirect]]
            [ring.util.response :refer [content-type file-response]]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [taoensso.timbre :refer [spy debug get-env]]))

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
  )
