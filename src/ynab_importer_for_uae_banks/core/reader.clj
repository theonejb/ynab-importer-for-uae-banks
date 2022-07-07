(in-ns 'ynab-importer-for-uae-banks.core)
(require '[clojure.java.shell :as shell]
         '[clojure.java.io :as io]
         '[clojure.string :as string])

(def XML2_DATA_LINE_PREFIX "/Workbook/Worksheet/ss:Table/Row/Cell/Data=")

(defn extract-cell-data-lines [input-path]
  (let [file (io/file input-path)
        xml2-sh-output (shell/sh "xml2" :in file)
        exit-status (:exit xml2-sh-output)
        cell-data-lines (string/split-lines (:out xml2-sh-output))]
    (if-not (= 0 exit-status)
      {:status :error :error-message (:err xml2-sh-output)}

      {:status          :success
       :cell-data-lines (->> cell-data-lines
                             (filter #(string/starts-with? % XML2_DATA_LINE_PREFIX))
                             (map #(string/replace-first % XML2_DATA_LINE_PREFIX "")))})))

(defn validate-cell-data-lines [statement-type-config cell-data-lines]
  (let [validation-lines (:validation-lines statement-type-config)
        count-validation-lines (count validation-lines)

        ; The partition function can also accept a step parameter. By default, it is equal to the length of each
        ; partition (arg 1) – which means that partitions don't overlap. But if you pass a step value smaller than the
        ; partition size, the partitions become overlapping. Using a step of 1 gives us a sliding window over our cell
        ; data lines. Each window is equal to the number of lines we need to match. If any of these windows matches
        ; the :validation-lines from our config, we have high confidence that the data comes from a bank statement
        ; matching the statement type the user passed.
        sliding-window-over-cell-data-lines (partition count-validation-lines 1 cell-data-lines)]
    (some #(= validation-lines %) sliding-window-over-cell-data-lines)))

(def statement-type-config-key->header-lines
  {
   :enbd-credit ["Transaction Date" "Posting Date" "Description" "Card Type" "Amount"]
   :enbd-debit  ["Date" "Description" "Debit" "Credit" "Account Balance"]
   })

(defn drop-until-sliding-window-matches
  "Removes elements from the start of coll until the first n items pred returns true for. The returned collection
  starts from the first element in the set of n items that matched."
  [n pred coll]
  (let [window (take n coll)]
    (cond
      (nil? coll) coll
      (pred window) coll
      :default (recur n pred (rest coll)))))

(defn remove-data-preceding-transactions [statement-type-config cell-data-lines]
  (let [header-lines (statement-type-config-key->header-lines (:statement-type statement-type-config))]
      (drop-until-sliding-window-matches (count header-lines) #(= header-lines %) cell-data-lines)))

(defn remove-non-transaction-data [statement-type-config cell-data-lines]
  (let [header-lines (statement-type-config-key->header-lines (:statement-type statement-type-config))
        cell-data-lines-starting-at-header (remove-data-preceding-transactions statement-type-config cell-data-lines)]
    (drop (count header-lines) cell-data-lines-starting-at-header)))