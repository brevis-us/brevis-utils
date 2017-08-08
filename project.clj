(defproject brevis.us/brevis-utils "0.1.2-SNAPSHOT"
  :description "Utilities from the Brevis suite"
  :url "https://github.com/brevis-us/brevis-utils"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]

                 [me.raynes/conch "0.8.0"]
                 
                 ;; Math
                 [net.mikera/core.matrix "0.48.0"]
                 [net.mikera/vectorz-clj "0.39.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-random "0.1.7"]]
  :repositories [["snapshots" {:url "https://clojars.org/repo"
                               :username :env/CI_DEPLOY_USERNAME
                               :password :env/CI_DEPLOY_PASSWORD
                               :sign-releases false}]
                 ["releases" {:url "https://clojars.org/repo"
                              :username :env/CI_DEPLOY_USERNAME
                              :password :env/CI_DEPLOY_PASSWORD
                              :sign-releases false}]])
