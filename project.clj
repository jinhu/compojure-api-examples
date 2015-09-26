(defproject jinhu/chimera-reasoning "0.1.0"
  :description "chimera.reasoning more than spike"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [lein-midje "3.1.3"]
                 [clj-time "0.11.0"] ;; needed as `lein ring uberwar` is broken.
                 [metosin/compojure-api "0.23.0"]]
  :ring {:handler chimera.reasoning.handler/app}
  :uberjar-name "examples.jar"
  :uberwar-name "examples.war"
  :profiles {:uberjar {:resource-paths ["swagger-ui"]
                       :aot :all}
             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.6"]]}})
