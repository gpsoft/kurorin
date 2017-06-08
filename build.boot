(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/cljc"}
  :resource-paths #{"html"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                  [org.clojure/clojurescript "1.9.562"]
                  [org.clojure/tools.reader "1.0.0-RC1"]  ;; for reading edn
                  [org.clojure/tools.nrepl "0.2.13"]
                  [adzerk/boot-cljs "2.0.0"]
                  [adzerk/boot-reload "0.5.1"]
                  [adzerk/boot-cljs-repl "0.3.2"]
                  [com.cemerick/piggieback "0.2.2"]
                  [weasel "0.7.0"]
                  [pandeiro/boot-http "0.8.3"]
                  [prismatic/dommy "1.1.0"]
                  [ring/ring-defaults "0.3.0"]
                  [cljs-ajax "0.6.0"]
                  [ring-json-response "0.2.0"]
                  [ring/ring-json "0.4.0"]
                  [enlive "1.1.6"]
                  [com.taoensso/timbre "4.10.0"]
                  [reagent "0.6.2"]
                  [re-frame "0.9.4"]
                  [bidi "2.1.1"]
                  [kibu/pushy "0.3.7"]
                  [com.datomic/datomic-free "0.9.5404"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(deftask dev
  "Start development."
  []
  (comp
    (serve :handler 'kurorin.core/app
           :resource-root "target"
           :reload true)
    (watch)
    (reload)
    (cljs-repl)
    (cljs)
    (target :dir #{"target"})))

