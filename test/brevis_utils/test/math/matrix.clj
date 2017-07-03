(ns brevis-utils.test.math.matrix
    (:use [clojure.test]
          [brevis-utils.math matrix])
    (:require [clojure.core.matrix :as mat]))

(deftest linear-solver
  (is (mat/equals (linear-solve [[3 2] [ 7 1]] [16 19])
                  [2 5])))
