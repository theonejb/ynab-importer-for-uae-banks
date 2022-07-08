(defproject ynab-importer-for-uae-banks "1.0"
  :description "Converts UAE bank statements into a CSV importable into YNAB."
  :url "https://github.com/theonejb/ynab-importer-for-uae-banks"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/data.csv "1.0.1"]]
  :main ^:skip-aot ynab-importer-for-uae-banks.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev     {:plugins [[lein-binplus "0.6.6"]]}}
  :bin {:name "ynab-importer-for-uae-banks"})
