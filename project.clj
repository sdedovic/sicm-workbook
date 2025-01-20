(defproject sicm-workbook "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :managed-dependencies [[com.taoensso/timbre "6.6.1"]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.mentat/emmy "0.32.0"]
                 [io.github.nextjournal/clerk "0.17.1102"]
                 [org.mentat/clerk-utils "0.6.0"]]
  :source-paths ["notebooks" "dev", "src"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
