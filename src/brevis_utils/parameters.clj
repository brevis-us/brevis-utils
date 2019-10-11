(ns brevis-utils.parameters
  (:use [clj-random.core :as random]))

(def params (atom {}))

; Consider making param functions :^dynamic

(defn clear-params
  "Clear the current params."
  []
  (reset! params {}))

(defn set-param
  "Set the value of a parameter."
  [param val & more]
  (swap! params assoc param val)
  (when-not (empty? more)
    (apply (partial set-param (first more) (second more))
           (drop 2 more))))

(defn get-param
  "Get the value of a parameter."
  [param]
  (get @params param))
   
(defn print-params
  "Print the current parameter map."
  ([]
   (print-params @params))
  ([ps]
   (doseq [[k v] ps]
     (cond (= k :random-seed)
           (println k (str "[" (random/seed-to-string v) "]"))
           :else
           (println k v)))))

(defn params-from-argseq
  "Load params from a sequence of arguments. Autoconverts strings, so this can be risky."
  [args]
  (let [;; First put everything into a map                                                                                                                                                                                                                                                                                 
        argmap (apply hash-map
                      (mapcat #(vector (read-string (first %)) (second %))
                              (partition 2 args)))
        ;; Then read-string on *some* args, but ignore others                                                                                                                                                                                                                                                              
        argmap (apply hash-map
                      (apply concat
                             (for [[k v] argmap]
                               [k (cond (= k :output-directory) v
                                        :else (read-string v))])))   
        random-seed (if (:random-seed argmap)
                      (byte-array (map byte (read-string (:random-seed argmap)))) 
                      (random/generate-mersennetwister-seed))]    
    (swap! params merge argmap)))

(defn write-params
  "Write the current params to the specified file. May not work happily with all param types yet."
  [filename]
  (spit filename @params))

(defn read-params
  "Read the params from the specified file. May not work happily with all param types yet."
  [filename]
  (let [in-params (slurp filename)]
    (reset! params (load-string in-params))))

(defn get-param-map
  "Get a map of parameters, maybe a subset specified by keys."
  ([]
   (get-param-map (keys @params)))
  ([ks]
   (select-keys @params ks)))
