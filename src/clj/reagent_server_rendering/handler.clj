(ns reagent-server-rendering.handler
  (:require [aleph.flow :as flow]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]])
  (:import [io.aleph.dirigiste Pools]
           [javax.script ScriptEngineManager Invocable]))


(defn- create-js-engine []
  (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
    ; React requires either "window" or "global" to be defined.
    (.eval "var global = this")
    (.eval (-> "public/js/server-side/app.js"
               io/resource
               io/reader))))

; We have one and only one key in the pool, it's a constant.
(def ^:private js-engine-key "js-engine")
(def ^:private js-engine-pool
  (flow/instrumented-pool
    {:generate   (fn [_] (create-js-engine))
     :controller (Pools/utilizationController 0.9 10000 10000)}))

(defn- render-page [page-id user-data]
  (let [js-engine @(flow/acquire js-engine-pool js-engine-key)]
    (try (.invokeMethod
           ^Invocable js-engine
           (.eval js-engine "reagent_server_rendering.core")
           "render_page" (object-array [page-id user-data]))
         (finally (flow/release js-engine-pool js-engine-key js-engine)))))

(defn js-env [page-id user-data]
  (if (System/getenv "PROD")
    ;; PROD:
    (list
      (include-js "/public/js/compiled/app.js")
      [:script {:type "text/javascript"}
        (str "reagent_server_rendering.core.main('" page-id "', " user-data ");")])
    ;; DEV: Figwheel JS
    (list
      (include-js "/public/js/figwheel/goog/base.js")
      (include-js "/public/js/figwheel/app.js")
      ;; This needs to be in its own script tag:
      [:script {:type "text/javascript"}
       "goog.require('reagent_server_rendering.core')"]
      ;; Now we know we have the core module:
      [:script {:type "text/javascript"}
       (str "reagent_server_rendering.core.main('" page-id "', " user-data ");")])

    ))

(defn page [page-id]
  (let [user-data (json/write-str {"counter" 10})]
   (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "/public/css/site.css" "http://code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.min.css")
     (include-js "https://code.jquery.com/jquery-1.11.2.min.js" "http://code.jquery.com/ui/1.11.2/jquery-ui.min.js")
     ]
    [:body
     [:div#app
      (render-page page-id user-data)]
      (js-env page-id user-data)]])
    )
  )

(defroutes app-routes
  (GET "/" [] (page "home"))
  ;; see reagent-server-rendering.core/pages
  ;; for a list of available pages
  (GET "/:page-id" [page-id] (page page-id))
  (resources "/public/")
  (not-found "Not Found"))

(def app (wrap-defaults app-routes site-defaults))
