(ns us.brevis.utils.test.parameters
  (:use [clojure.test])
  (:require [us.brevis.utils.parameters :as params]))

(deftest test-set-param
  (let [v 42]
    (params/set-param :test v)
    (is (= (params/get-param :test) v))))

(deftest test-param-file-io
  (let [test-params {:integer-test 3
                     :double-test 13.0}]; yeah string test i know i know

    (params/clear-params)
    (doseq [[k v] test-params]
      (params/set-param k v))
    (params/write-params "test-params.clj")
    (params/read-params "test-params.clj")
    (is (= test-params @params/params))))
