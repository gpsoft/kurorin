(set-env!
  :source-paths #{"src/clj" "src/cljs" "src/cljc"}
  :resource-paths #{"html"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                  [org.clojure/clojurescript "1.9.562"]
                  [org.clojure/tools.reader "1.0.0"]  ;; for reading edn
                  [org.clojure/tools.nrepl "0.2.13"]
                  [adzerk/boot-cljs "2.0.0"]
                  [adzerk/boot-reload "0.5.1"]
                  [adzerk/boot-cljs-repl "0.3.2"]
                  [com.cemerick/piggieback "0.2.2"]
                  [weasel "0.7.0"]
                  [pandeiro/boot-http "0.8.3"]
                  [prismatic/dommy "1.1.0"]
                  [cheshire "5.7.1"]
                  [ring/ring-core "1.6.1"]
                  [ring/ring-jetty-adapter "1.6.1"]
                  [ring/ring-defaults "0.3.0"]
                  [ring-json-response "0.2.0"]
                  [ring/ring-json "0.4.0"]
                  [cljs-ajax "0.6.0"]
                  [enlive "1.1.6"]
                  [com.taoensso/timbre "4.10.0"]
                  [reagent "0.6.2"]
                  [re-frame "0.9.4"]
                  [re-frisk "0.4.5"]
                  [compojure "1.6.0"]
                  [bidi "2.1.1"]
                  [kibu/pushy "0.3.7"]
                  [clj-http "3.6.1"]
                  [cprop "0.1.10"]
                  [cljsjs/jquery-ui "1.11.4-0"]
                  [com.draines/postal "2.0.2"]
                  [com.google.guava/guava "22.0"] ;; for datomic
                  [com.datomic/datomic-free "0.9.5404"]])

;; cprop reads from this.
;; or java -Dconf="/path/to/conf.edn" -jar kurorin.jar in production
(System/setProperty "conf" "./app-config.edn")

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[taoensso.timbre :refer [set-level! merge-config!]]
         '[taoensso.timbre.appenders.core :refer [spit-appender]])

(task-options!
  pom {:project 'kurorin
       :version "0.1.0-SNAPSHOT"}
  aot {:namespace '#{kurorin.core}}
  jar {:main 'kurorin.core
       :file "kurorin.jar"
       :manifest {"Description" "Compose GitHub readmes and publish them in Kindle mobi file"
                  "url" "http://localhost"}})

(deftask with-logfile
  []
  (set-level! :debug)
  (merge-config!
    {:appenders
     {:spit (spit-appender {:fname "/tmp/kurorin.log"})}})
  identity)

(deftask dev
  "Start development."
  []
  (comp
    (with-logfile)
    (serve :handler 'kurorin.core/app
           :resource-root "target"
           :reload true)
    (watch)
    (reload)
    (cljs-repl)
    (cljs)
    (target :dir #{"target"})))

(deftask release
  []
  (comp
    (cljs :optimizations :advanced
          :compiler-options {:preloads nil})
    (aot)
    (pom)
    (uber)
    (jar)
    (target :dir #{"release"})))
