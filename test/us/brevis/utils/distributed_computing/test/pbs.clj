(ns us.brevis.utils.distributed-computing.test.pbs
    (:gen-class)
    (:require [clojure.test :refer :all]
              [us.brevis.utils.distributed-computing.pbs :refer :all]
              [us.brevis.utils.parameters :as params]))

(defn -main
  [& args]
  (let [argmaps (params/params-from-argseq args)]
    (println "PBS test job:" *ns* (System/getenv "HOSTNAME"))
    (println argmaps)))

(deftest test-PBS-launch
    (when (System/getenv "PBS_BREVISTEST")
      (let [argmaps (into []
                          (map #(hash-map :run-id %)
                               (range 2)))
            namespace "us.brevis.utils.distributed-computing.test.pbs"
            expName "Brevis_PBSTest"
            username (System/getenv "PBS_USERNAME")
            server (System/getenv "PBS_HOSTNAME")
            numruns 2
            source (System/getenv "PBS_SOURCEDIR")
            destination "~/brevis_dir/"
            optArgs (System/getenv "PBS_JOBLAUNCHARGS")]
        (start-runs argmaps namespace expName username server numruns source destination optArgs))))



