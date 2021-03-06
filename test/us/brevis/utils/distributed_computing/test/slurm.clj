(ns us.brevis.utils.distributed-computing.test.slurm
  (:gen-class)
  (:require [clojure.test :refer :all]
            [us.brevis.utils.distributed-computing.slurm :refer :all]
            [us.brevis.utils.parameters :as params]))

(defn -main
  [& args]
  (let [argmaps (params/params-from-argseq args)]
    (println "Slurm test job:" *ns* (System/getenv "HOSTNAME"))
    (println argmaps)))

(deftest test-slurm-launch
  (when (System/getenv "SLURM_BREVISTEST")
    (let [argmaps (into []
                        (map #(hash-map :run-id %)
                             (range 2)))
          namespace "us.brevis.utils.distributed-computing.test.slurm"
          expName "Brevis_SLURMTest"
          username (System/getenv "SLURM_USERNAME")
          server (System/getenv "SLURM_HOSTNAME")
          numruns 2
          source (System/getenv "SLURM_SOURCEDIR")
          destination "~/brevis_dir/"
          optArgs (System/getenv "SLURM_JOBLAUNCHARGS")]
      (start-runs argmaps namespace expName username server numruns source destination optArgs))))



