(ns kurorin.core
  (:require [kurorin.views]
            [kurorin.routes]
            [kurorin.events]
            [bidi.bidi :as bidi]
            [reagent.core :refer [render]]
            [re-frame.core :as r]
            [re-frisk.core]
            [dommy.core :as dom]
            [taoensso.timbre :refer-macros [spy debug get-env]]))

(debug "Starting...")

(r/reg-sub
  :current-page
  (fn [db _]
    (:current-page db)))

(defn mount-root []
  (r/clear-subscription-cache!)
  (re-frisk.core/enable-re-frisk!)
  (render [kurorin.views/main-ui]
          (dom/sel1 :#content)))

(defn init
  []
  (kurorin.routes/app-routes)
  (r/dispatch-sync [:initialize-db])
  (mount-root))

