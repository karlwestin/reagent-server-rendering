(ns reagent-server-rendering.pages
  (:require [reagent.core :as reagent]))


(defn home-page []
  [:div [:h2 "Welcome to reagent-server-rendering"]])

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
    [:input#tags]]])

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
      [:span {:style {:color "red"}} "Red text!"]]])

