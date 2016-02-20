(ns reagent-server-rendering.pages
  (:require [reagent.core :as reagent]
            [alandipert.storage-atom :refer [local-storage]]))

(enable-console-print!)

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


(defn update-highlighter
  "displays 2 vals, hightlights the most recent updated"
  [val1 val2]
  (let [last-updated (reagent/atom 0)]
    (reagent/create-class
     {:component-will-update (fn [this new-argv]
                              (let [[_ old-v1 old-v2] (reagent/argv this)
                                    [_ new-v1 new-v2] new-argv]
                                (reset! last-updated
                                        (cond
                                          (> new-v1 old-v1) 1
                                          (> new-v2 old-v2) 2
                                          :else 0)) ) )
     :reagent-render (fn [val1 val2]
                       [:div
                        [:div (when (= 1 @last-updated ) {:class "selectedRow"})
                         (str "val1=" val1) ]
                        [:div (when (= 2 @last-updated ) {:class "selectedRow"})
                         (str "val2=" val2)] ])})))

(defn argv-page []
  (let [v1 (reagent/atom 0)
        v2 (reagent/atom 0)]
   (fn []
          [:div
           [:button {:on-click #(swap! v1 inc)} "inc val1"]
           [:button {:on-click #(swap! v2 inc)} "inc val2"]
           [update-highlighter @v1 @v2] ])))

(defonce app-state
  (try
    (local-storage (reagent/atom {:counter 0}) :app-state)
    (catch js/Object e
      (reagent/atom {:counter 0}))))

(defn storage-page []
  [:div
   [:h1 "this page is driven with local storage"]
   [:div "Current count: " (@app-state :counter)]
   [:button {:on-click (fn [e] (swap! app-state update-in [:counter] inc))} "increment"]
   ])
