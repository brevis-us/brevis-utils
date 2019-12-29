(defproject us.brevis/brevis-utils "1.0.0-SNAPSHOT"
  :description "Utilities from the Brevis suite"
  :url "https://github.com/brevis-us/brevis-utils"
  :license {:name "Apache v2"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [me.raynes/conch "0.8.0"]
                 
                 ;; Math
                 [net.mikera/core.matrix "0.48.0"]
                 [net.mikera/vectorz-clj "0.39.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [com.kephale/clj-random "21e3dd4168"]]
  :repositories [["jitpack" "https://jitpack.io"]
                 ["snapshots" {:url "https://clojars.org/repo"
                                           :username :env/CI_DEPLOY_USERNAME
                                           :password :env/CI_DEPLOY_PASSWORD
                                           :sign-releases false}]
                 ["releases" {:url "https://clojars.org/repo"
                              :username :env/CI_DEPLOY_USERNAME
                              :password :env/CI_DEPLOY_PASSWORD
                              :sign-releases false}]])
