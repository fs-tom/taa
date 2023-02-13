(defproject taa "0.0.9-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 ;[proc "0.3.0-SNAPSHOT"]
                 [marathon "4.2.7-SNAPSHOT"]
                 ;;not in clojars either, this is fs-c's fork.
                 [smiletest "0.1.0-SNAPSHOT"]
                 ;[com.clojure-goes-fast/clj-memory-meter "0.2.1"]
                 ;[techascent/tech.ml.dataset "7.000-beta-2"]
                 ]
  :repl-options {:init-ns taa.core
                 :timeout 120000}

  ;;allow testing ns and data to be resolved for transitive dep.
  :source-paths   ["src" "test" "../m4/src/"]
  :resource-paths ["test/resources"]
  :profiles {;;load our tests from resources just like in the uberjar
             :dev {:resource-paths ["test/resources"]
                   :jvm-opts ^:replace ["-Xmx8g"]
                   :source-paths []
                   }}
  :plugins [[reifyhealth/lein-git-down "0.4.1"]]
  :middleware [lein-git-down.plugin/inject-properties]
  :repositories [["public-github" {:url "git://github.com"}]]
  :git-down {marathon  {:coordinates  fsdonks/m4}
             smiletest {:coordinates  fs-tom/krigingdemo}})
