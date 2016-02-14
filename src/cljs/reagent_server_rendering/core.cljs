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
      [:li [item "/autocomplete" "autocomplete" active]] ])

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

  (let [history (hook-browser-navigation!)
        nav! (fn [token]
               (println "calling html5 navigation" token)
               (.setToken history token))]
    (swap! app-state assoc :navigate nav!)))

(def pages
  {"home"  pages/home-page
   "about" pages/about-page
   "autocomplete" pages/auto-page})

(defmulti current-page #(@app-state :page))
(defmethod current-page "home" []
  (println "rendering home")
  [:div
   [pages/home-page]
   [menu "/"]])
(defmethod current-page "about" []
  (println "rendering about")
  [:div
   [pages/about-page]
   [menu "/about"]])
(defmethod current-page "autocomplete" []
  (println "rendering autocomplete")
  [:div
   [pages/auto-page]
   [menu "/autocomplete"]])
(defmethod current-page :default []
  (println "rendering default")
  [:div "default page" [menu "/404"]])

(defn ^:export render-page [page-id]
  (reagent/render-to-string [(get pages page-id)]))

(defn ^:export main [page-id]
  (swap! app-state assoc :page page-id)
  (app-routes)
  (reagent/render [current-page] (.getElementById js/document "app")))
