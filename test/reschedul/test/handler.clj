(ns reschedul.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [reschedul.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))


;(def cp {:curr {:q1 false :q2 true :q3 false}})
;(defn aggreement-signable? []
;  (not (some #(= false %)
;             (map
;               (fn [q]
;                 (get-in cp [:curr q]))
;               (keys (get-in cp [:curr]))))))
