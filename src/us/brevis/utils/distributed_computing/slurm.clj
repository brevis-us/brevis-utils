(ns us.brevis.utils.distributed-computing.slurm
  (:use [clojure.java.shell]
        [clojure.math.numeric-tower]
        [us.brevis.utils.distributed-computing.dc-utils])
  (:require [me.raynes.conch :refer [programs with-programs let-programs]]
            [clojure.string :as string])
  (:import [java.io]))

(def debug false)

;; Function that takes a list of argmaps and generates and starts runs for the cluster
;; (ssh call)
;; Later: hook class that lets you listen to system signals -> dump to serialized files

(defn gen-config
  "Takes a set of params(argmap), a configFileName, and the output
parameters to be used in the output log, then generates the
appropriate configuration file to be passed to the hpc."
  [argmap configFileName namespace expName basedir]
  (let [out-str (str
                  "#!/bin/sh\n"
                  "cd " basedir ";\n"
                  "lein run -m "
                  namespace
                  #_expName
                  " "
                  (serialize-map argmap " ")
                  "\n"
                  #_" > "
                  ;expName "_" configFileName "_run$SGE_TASK_ID" ".log"
                  #_".log")]
    (when debug (println "gen-config:" configFileName out-str))    
    (spit configFileName out-str)))

(defn gen-command
  "Takes a set of params(argmap), a configFileName, and the output
parameters to be used in the output log, then generates the
appropriate configuration file to be passed to the hpc."
  [argmap namespace basedir profile-name]
  (let [out-str (str
                  "cd " basedir "; "
                  "lein "
                  (if profile-name (str "with-profile " profile-name " ") "")
                  "run -m "
                  namespace
                  #_expName
                  " "
                  (serialize-map argmap " "))]
    (when debug (println "command:"  out-str))    
    out-str))

(defn launch-config
  "Launches an experiment from the configuration file numruns times."
  [username server expName configFile numruns duration]
  (let [command (str "sbatch --time=" duration " -J " expName " -n " (str numruns) " " configFile)]
    #_(str "bsub " optArgs " -t 1-" (str numruns) " -N " expName " " configFile)
    (when debug (println "launch-config:" command))
    (remote-command username server command)))

(defn start-runs
  [argmaps namespace expName username server numruns source destination opt-args]
  (loop [conf 0
         argmaps argmaps]
    (when-not (empty? argmaps)
          (gen-config (first argmaps) (str "job_" expName "_" conf ".sh") namespace expName (str destination expName))
          (recur (inc conf)
                 (rest argmaps))))
  (upload-files username server (str source "/") (str destination expName "/"))
  (println "Uploaded files.")
  (Thread/sleep 0.01)
  (println "Remotely configuring project.")
  (remote-command username server (str "cd " destination expName "; lein clean; lein compile;"))
  (println "Configuration complete.")
  (dotimes [i (count argmaps)]
    (upload-files username server (str "job_" expName "_" i ".sh") (str destination expName "/" (str "job_" expName "_" i ".sh")))
    (launch-config username server expName (str destination expName "/job_" expName "_" i ".sh") numruns opt-args)
    (Thread/sleep 0.1)
    #_(Thread/sleep 0.01))
  (println "All runs submitted."))

(defn launch-array
  "Launches an experiment from the configuration file numruns times."
  [username server expName jobFile numjobs extra-args enable-job-output]
  (let [command (str "source /etc/profile; sbatch " (when-not enable-job-output "-o /dev/null ") " " extra-args " -J " expName " -n " (str numjobs) " " jobFile)]
    #_(str "bsub " optArgs " -t 1-" (str numruns) " -N " expName " " configFile)
    (when debug (println "launch-config:" command))
    (remote-command username server command)))


(defn start-run-array
   [argmaps namespace username server & {:keys [expName numruns source destination extra-args profile-name with-cleanup enable-job-output
                                                copy-entire-project]
                                         :or {expName (str "brevis_experiment_" (System/nanoTime))
                                              numruns 1
                                              source "./"
                                              destination "~/"
                                              extra-args "--time=01:00:00"
                                              profile-name nil ;"cluster"
                                              with-cleanup false
                                              enable-job-output true
                                              copy-entire-project true}}]
   (let [command-list (for [run-id (range numruns)
                            argmap argmaps]; this could be a good time to insert unique random seeds
                        (gen-command argmap namespace (str destination expName) profile-name))
         command-filename (str expName "_commands.sh")
         job-filename (str expName "_job.sh")
         max-jobs 1000]
     ;; Write command list
     (spit command-filename
           (string/join "\n" command-list)); this could be a good time to insert unique random seeds
     ;; Write command list job
     (if (> (count command-list) max-jobs)
       (spit job-filename
             (str "#!/bin/bash\nsource ~/.bashrc\n"
                  (string/join "\n"
                               (for [rep (range (ceil (/ (count command-list) max-jobs)))]
                                 (str "sed -n -e ''$(($SLURM_ARRAY_TASK_ID+" rep  "*" max-jobs "))'p' """ (str destination expName "/" command-filename) " | sh")))))
       (spit job-filename
             (str "#!/bin/sh\nsource ~/.bashrc\n                                                                                                                                                                                         
sed -n -e \"$SLURM_ARRAY_TASK_ID p\" " (str destination expName "/" command-filename) " | sh")))
     (when debug "Uploading files")
     (if copy-entire-project
       (upload-files username server (str source "/") (str destination expName "/"))
       (let [to-copy ["src" "project.clj" "resources" command-filename job-filename]]
         (doseq [f to-copy]           
           (upload-files username server (str source "/" f) (str destination expName "/")))))
     (println "Uploaded files.")
     (Thread/sleep 0.01)
     (println "Remotely configuring project.")
     (when with-cleanup
       (println "Cleanup!")
       (remote-command username server (str "cd " destination expName "; lein clean; lein compile;")))
     (println "Configuration complete.")
     (launch-array username server expName (str destination expName "/" job-filename) (min max-jobs (count command-list)) extra-args enable-job-output)
     (println "All runs submitted.")))

