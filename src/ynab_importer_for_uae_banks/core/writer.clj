(in-ns 'ynab-importer-for-uae-banks.core)

(require '[clojure.data.csv :as csv]
         '[clojure.java.io :as io])

(defn transaction-map->ynab-format-coll [transaction-map]
  (let [{:keys [date description amount]} transaction-map]
    [(.toString date) description amount]))

(defn write-cleaned-data [output-path cleaned-data]
  (let [data-in-ynab-format (map transaction-map->ynab-format-coll cleaned-data)
        data-with-header (cons ["Date" "Payee" "Amount"] data-in-ynab-format)]
    (with-open [writer (io/writer output-path)]
      (csv/write-csv writer data-with-header))))