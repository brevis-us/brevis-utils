(ns brevis-utils.distributed-computing.dc-utils  
   (:use [clojure.java.shell]
        [clojure.math.numeric-tower])
  (:require [me.raynes.conch :refer [programs with-programs let-programs]]
            [clojure.string :as string])
  (:import [java.io]))

(def debug-mode (atom false))
(programs ssh)

#_(defn local-command
   [command]
   (when debug (println "local-command:" command))
   #_(sh command)
   (.exec (. Runtime getRuntime) command))

(defn local-command
  "Default behavior is a nonblocking execution."
  ([command]
    (local-command command nil))
  ([command timeout]
    (when @debug-mode (println "local-command:" command "with timeout:" timeout))
    (let [process (.exec (. Runtime getRuntime) command)]
      (when timeout
        (.waitFor process timeout java.util.concurrent.TimeUnit/MILLISECONDS)))))

(defn remote-command
  [username server command]
  (let [comm #_(str "ssh " username "@" server " \\\"" command "\\\"")
        (str "ssh " username "@" server " \"" command "\"")]
    (when @debug-mode (println "remote-command:" comm))
    (ssh (str username "@" server) command)
    #_(local-command comm)))

(defn upload-files
  "Upload files from source to server:destination. Blocks until upload is complete."
  [username server source destination]
  ;; source: ../../../
  ;; destination: ~/
  (let [command (str "rsync -avzr " source " " username "@" server ":" destination)]
    (when @debug-mode (println "upload-files:" command))    
    (local-command command true)))

(defn serialize-map 
  [m sep] 
  (str (clojure.string/join sep (map (fn [[k v]] (str k " " v)) m ))))
