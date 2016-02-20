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

;; To create a page, add it here
;; (adds page both server and client side)
(def pages
  {"home"  pages/home-page
   "about" pages/about-page
   "compare-argv" pages/argv-page
   "server-data" pages/server-data
   "autocomplete" pages/auto-page
   "404" pages/not-found })

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
      [:li [item "/server-data" "Server Data" active]]
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


;; Sanity check if wanted page is defined in pages
(defn ^:export get-page [wanted-id]
  (if (get pages wanted-id)
    wanted-id
    "404"))

(defn app-routes []
  (defroute "/" []
    (swap! app-state assoc :page "home"))

  (defroute "/:page" {:as params}
    ;; check if the page is defined above
    (swap! app-state assoc :page (:page params)))

  (let [history (hook-browser-navigation!)
        nav! (fn [token]
               (println "calling html5 navigation" token)
               (.setToken history token))]
    (swap! app-state assoc :navigate nav!)))

(defn page [component route]
 [:div
   [component]
   [menu route]])

(defn render-client-side []
  (let [page-id (@app-state :page)
        checked (get-page page-id)]
    (page (get pages checked) page-id)))

;; Server-Side rendering starting point
(defn ^:export render-page [page-id user-data]
  (let [user-data (js->clj (.parse js/JSON user-data))]
    (reset! pages/app-state user-data)
    (reagent/render-to-string [page (get pages (get-page page-id)) (str "/" page-id)])))

;; Client side rendering starting point
(defn ^:export main [page-id user-data]
  (println user-data)
  (let [user-data (js->clj user-data)]
  ;; routing
    (swap! app-state assoc :page page-id)
    ;; user data
    (reset! pages/app-state user-data)
    (app-routes)
    (reagent/render [render-client-side] (.getElementById js/document "app"))))
