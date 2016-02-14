(ns reagent-server-rendering.core
    (:require [reagent.core :as reagent]))

(defn item [link title active]
  (if (= link active)
    [:span title]
    [:a {:href link} title]))

(defn menu [active]
  [:ul
      [:li [item "/" "home" active]]
      [:li [item "/about" "about" active]]
      [:li [item "/autocomplete" "autocomplete" active]] ])

(defn home-page []
  [:div [:h2 "Welcome to reagent-server-rendering"]
   [menu "/"]])

(def auto-tags
  ["ActionScript"
   "AppleScript"
   "Asp"
   "BASIC"
   "C"
   "C++"
   "Clojure"
   "COBOL"
   "ColdFusion"
   "Erlang"
   "Fortran"
   "Groovy"
   "Haskell"
   "Java"
   "JavaScript"
   "Lisp"
   "Perl"
   "PHP"
   "Python"
   "Ruby"
   "Scala"
   "Scheme"])

(defn auto-did-mount []
  (js/$ (fn []
          (.autocomplete (js/$ "#tags")
                         (clj->js {:source auto-tags})))))

(defn auto-render []
  [:div [:h2 "this is the auto complete page"]
   [:div.ui-widget
    [:label {:for "tags"} "Programming Languages"]
    [:input#tags]]
   [menu "/autocomplete"]])

;; This is who to create a normal react class with lifecycle methods
(defn auto-page []
  (reagent/create-class {:reagent-render auto-render
                         :component-did-mount auto-did-mount}))

(defn simple-component []
  [:div "im a simple component"])

(defn about-page []
  [:div [:h2 "About reagent-server-rendering"]
   [simple-component]
   [:div
      "Some different "
      [:strong "bold"]
      " text. "
      [:span {:style {:color "red"}} "Red text!"]]
   [menu "/about"]])

(def pages
  {"home"  home-page
   "about" about-page
   "autocomplete" auto-page})

(defn ^:export render-page [page-id]
  (reagent/render-to-string [(get pages page-id)]))

(defn ^:export main [page-id]
  (reagent/render [(get pages page-id)] (.getElementById js/document "app")))
