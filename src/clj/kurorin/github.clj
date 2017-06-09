(ns kurorin.github
  (:require [cprop.core :refer [load-config]]
            [clj-http.client :as http]
            [cheshire.core :refer [parse-string]]
            [taoensso.timbre :refer [spy debug get-env]]))

(def cfg (load-config))
(def url-base "https://api.github.com")
(def opt-base {:basic-auth (:github-credential cfg)})

(defn- fetch
  [ep opt json?]
  (debug (get-env))
  (let [body (:body (http/get ep opt))]
    (if json?
      (parse-string true)   ;; true: keywordised key
      body)))

(defn search-user
  "Search users. Result is in JSON."
  [kw]
  (let [ep (str url-base "/search/users?q=" kw)]
    (fetch ep opt-base true)))

(defn search-repo
  "Search repositories. Result is in JSON."
  [kw]
  (let [ep (str url-base "/search/repositories?q=" kw)]
    (fetch ep opt-base true)))

(defn fetch-readme
  "Fetch readme of the repo. Result is in partial HTML string."
  [user repo]
  (let [ep (str url-base "/repos/" user "/" repo "/readme")]
    (fetch ep (assoc opt-base
                     :headers
                     {:Accept "application/vnd.github.v3.html+json"})
           false)))

(comment
  (search-user "gpsoft")
  (search-repo "neko")
  (fetch-readme "cgrand" "enlive")
  )
