(ns ^:figwheel-no-load reschedul.app
  (:require [reschedul.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :on-jsload core/mount-components)
(.log js/console "INIT")
(core/init!)
