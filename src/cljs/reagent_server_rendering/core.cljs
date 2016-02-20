(ns reagent-server-rendering.core
    (:require-macros [secretary.core :refer [defroute]])
    (:import goog.history.Html5History)
    (:require [secretary.core :as secretary]
              [reagent-server-rendering.pages :as pages]
              [reagent.core :as reagent]
              [goog.history.EventType :as EventType]
              [goog.events :as events]))

(enable-console-print!)

(defonce app-state (reagent/atom {}))

(defn item [link title active]
  (let [nav! (@app-state :navigate)]
    (if (= link active)
      [:span title]
      [:a {:href link
           :onClick (fn [e]
                      (.preventDefault e)
                      (nav! link))}
       title])))

(defn menu [active]
  [:ul
      [:li [item "/" "home" active]]
      [:li [item "/about" "about" active]]
      [:li [item "/autocomplete" "autocomplete" active]]
      [:li [item "/local-storage" "Local Storage" active]]
      [:li [item "/compare-argv" "Compare argv tutorial" active]]  ])

;; Client side routing with html5 pushstate
;; is a mix of this: http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history
;; and this: https://github.com/reagent-project/reagent-cookbook/tree/master/recipes/add-routing

(defn get-token []
  (str js/window.location.pathname js/window.location.search))

(defn handle-url-change [e]
  (js/console.log (str "Navigating: " (get-token)))
  ;; we are checking if this event is due to user action,
  ;; such as click a link, a back button, etc.
  ;; as opposed to programmatically setting the URL with the API
  (when-not (.-isNavigation e)
    ;; in this case, we're setting it
    (js/console.log "Token set programmatically")
    ;; let's scroll to the top to simulate a navigation
    (js/window.scrollTo 0 0))
  ;; dispatch on the token
  (secretary/dispatch! (get-token)))

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (.setPathPrefix (str js/window.location.protocol
                         "//"
                         js/window.location.host))
    (.setUseFragment false)
    (.setEnabled true)
    (events/listen
     EventType/NAVIGATE
     handle-url-change)))

;; (defn nav! [token]
;; (.setToken history token))

(defn app-routes []
  (defroute "/" []
    (swap! app-state assoc :page "home"))

  (defroute "/about" []
    (swap! app-state assoc :page "about"))

  (defroute "/autocomplete" []
    (swap! app-state assoc :page "autocomplete"))

  (defroute "/compare-argv" []
    (swap! app-state assoc :page "compare-argv"))

  (defroute "/local-storage" []
    (swap! app-state assoc :page "local-storage"))

  (let [history (hook-browser-navigation!)
        nav! (fn [token]
               (println "calling html5 navigation" token)
               (.setToken history token))]
    (swap! app-state assoc :navigate nav!)))

(def pages
  {"home"  pages/home-page
   "about" pages/about-page
   "compare-argv" pages/argv-page
   "local-storage" pages/storage-page
   "autocomplete" pages/auto-page})

(defn page [component route]
 [:div
   [component]
   [menu route]])

(defn render-client-side []
  (let [page-id (@app-state :page)]
    (page (get pages page-id) page-id)))

;; Server-Side rendering starting point
(defn ^:export render-page [page-id]
  (reagent/render-to-string [page (get pages page-id) (str "/" page-id)]))

;; Client side rendering starting point
(defn ^:export main [page-id]
  (swap! app-state assoc :page page-id)
  (app-routes)
  (reagent/render [render-client-side] (.getElementById js/document "app")))
