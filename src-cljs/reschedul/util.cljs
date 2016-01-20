(ns reschedul.util
  (:import goog.History)
  (:require [reschedul.session :as session]
            [goog.crypt.base64 :as b64]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [clojure.string :refer [join trim]]
            [secretary.core :as secretary
             :include-macros true]
            [ajax.core :as ajax]))

(defn millis []
  (.getTime (js/Date.)))

(defn GET [url & [opts]]
  (ajax/GET (str js/context url) (update-in opts [:params] assoc :timestamp (millis))))

(defn POST [url opts]
  (ajax/POST (str js/context url) opts))

(defn text [id]
  (session/get-in [:locale id]))

(defn hook-browser-navigation!
  "hooks into the browser's navigation (e.g. user clicking on links, redirects, etc) such that any
   of these page navigation events are properly dispatched through secretary so appropriate routing
   can occur. should be called once on app startup"
  []
  ;(let [h (History.)
  ;      f (fn [he] ;; goog.History.Event
  ;          (.log js/console "navigate %o" (clj->js he))
  ;          (let [token (.-token he)]
  ;            (if (seq token) ;; preferred over (not (empty? token))
  ;              (secretary/dispatch! token))))]
  ;  (events/listen h EventType/NAVIGATE f)
  ;  (doto h (.setEnabled true))))

  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn format-title-url [id title]
  (when title
    (->> (re-seq #"[a-zA-Z0-9]+" title)
         (clojure.string/join "-")
         (js/encodeURI)
         (str id "-"))))

(defn url [parts]
  (if-let [context (not-empty js/context)]
    (apply (partial str context "/") parts)
    (apply str parts)))

(defn set-location! [& url-parts]
  (set! (.-href js/location) (url url-parts)))

(defn set-venues-url [{:keys [id]}]
  (set-location! "#/venues/" id))


(defn set-page! [page]
  (session/put! :page page))

(defn set-admin-page! [page]
  (if (session/get :admin)
    (set-page! page)
    (set-location! "/")))

(defn set-title! [title]
  (set! (.-title js/document) title))


(defn mounted-component [component handler]
  (with-meta
    (fn [] component)
    {:component-did-mount
     (fn [this]
       (let [node (reagent.core/dom-node this)]
         (handler node)))}))

(defn html [content]
  [(mounted-component
     [:div {:dangerouslySetInnerHTML
            {:__html content}}]
     #(let [nodes (.querySelectorAll % "pre code")]
       (loop [i (.-length nodes)]
         (when-not (neg? i)
           (when-let [item (.item nodes i)]
             (.highlightBlock js/hljs item))
           (recur (dec i))))))])

(defn markdown [text]
  (-> text str js/marked html))

(defn input-value [input]
  (-> input .-target .-value))

(defn set-value! [target]
  (fn [source] (reset! target (input-value source))))

(defn text-input [target & [opts]]
  [:input (merge
            {:type "text"
             :on-change (set-value! target)
             :value @target}
            opts)])

(defn link [& [x y & xs :as body]]
  (if (map? x)
    [:a (merge {:href (url y)} x) xs]
    [:a {:href (url x)} (rest body)]))

(defn nav-link [path label & [on-click]]
  [:li {:on-click on-click} (link path (text label))])





(defn error-handler [resp] ; [{:keys [status status-text]}]
  (.log js/console
        (str "something bad happened: " resp))) ;" status " " status-text)))

(defn empty-all-string-values [m]
  (let [res (reduce-kv (fn [m k _]
                         (assoc m k "")) {} m)]
    res))

;(assert (= (empty-all-string-values {:a "foo"}) {:a ""}))

(defn trim-list-of-strings [vnames]
  (let [trimmed (map #(trim (str %)) vnames)]
    ;(.log js/console (str trimmed))
    trimmed))

;(assert (= (trim-list-of-strings ["foo/goo.ml" " foo/goo.ml " "foo/goo.ml   "])
;          (("foo/goo.ml" "foo/goo.ml" "foo/goo.ml"))))