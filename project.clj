(defproject reagent-server-rendering "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [reagent "0.5.1"]
                 ;;Ring is needed to start an HTTP server
                 [ring "1.4.0"]
                 [compojure "1.4.0"]
                 ;;provides default middleware for Ring
                 [ring/ring-defaults "0.1.5"]
                 ;;server-side HTML templating
                 [hiccup "1.0.5"]
                 ;;thread pool management
                 [aleph "0.4.0"]
                 ;; client side routing
                 [secretary "1.2.3"]
                 ;; local storage
                 [alandipert/storage-atom "1.2.4"]
                 [org.clojure/data.json "0.2.6"]
                 [lein-figwheel "0.5.7"]]

  :source-paths ["src/clj"]

  :ring {:handler reagent-server-rendering.handler/app}

  :plugins [[lein-cljsbuild "1.0.6"]
            ;;plugin for starting the HTTP server
            [lein-ring "0.9.6"]
            [lein-figwheel "0.5.7"]]

  :clean-targets ^{:protect false} ["resources/public/js" "target" "test/js"]

  :cljsbuild {:builds [{:id "server-side"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/server-side/app.js"
                                   :output-dir "resources/public/js/server-side"
                                   ;; all files inlined
                                   :optimizations :whitespace}}
                       {:id "prod"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled"
                                   ;; all files inlined
                                   :optimizations :advanced}}
                       {:id "hmr"
                        :source-paths ["src/cljs"]
                        :figwheel { :on-jsload "reagent-server-rendering.core/figwheel-reload" }
                        :compiler {:output-to "resources/public/js/figwheel/app.js"
                                   :output-dir "resources/public/js/figwheel"
                                   :asset-path "js/figwheel"
                                   :optimizations :none
                                   :pretty-print true}}]})
