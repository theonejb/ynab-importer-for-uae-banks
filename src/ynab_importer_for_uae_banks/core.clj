(ns ynab-importer-for-uae-banks.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            clojure.pprint)
  (:gen-class))

(load "core/reader")
(load "core/cleaner")
(load "core/writer")

; To make clj-kondo happy about things declared in the above files.
(declare extract-cell-data-lines)
(declare validate-cell-data-lines)
(declare remove-non-transaction-data)
(declare transaction-data-lines->maps)
(declare write-cleaned-data)

(defn get-config-path []
  (str (System/getProperty "user.home") \/ ".config/ynab-importer-for-uae-banks.edn"))

(defn load-config
  [path]
  (edn/read-string (slurp path)))

(def statement-type->config-key {"ENBD-Debit" :enbd-debit "ENBD-Credit" :enbd-credit})
(defn is-valid-statement-type? [user-input]
  (not (nil? (statement-type->config-key user-input))))

(defn does-file-exist? [path] (.exists (io/file path)))

(defn is-xml2-installed? [] (= 0 (:exit (shell/sh "which" "xml2"))))

(defn exit-with-error [& error-parts]
  (println (apply str error-parts))
  (System/exit 1))

(defn check-preconditions-with-exit-on-error [config-path statement-type-user-input input-path output-path]
  (cond
    (not (does-file-exist? config-path))
    (exit-with-error "Config file not found. Please ensure a config file is created at '" config-path "'.")

    (not (is-valid-statement-type? statement-type-user-input))
    (exit-with-error "Invalid statement type '" statement-type-user-input "'. Valid values are: ENBD-Debit ENBD-Credit.")

    (not (does-file-exist? input-path))
    (exit-with-error "Input file does not exist. Provided path: '" input-path "'.")

    (does-file-exist? output-path)
    (exit-with-error "Output file already exists. Provided path: '" output-path "'.")

    (not (is-xml2-installed?))
    (exit-with-error "xml2 not found. Please make sure it's installed and available on your $PATH")

    :default
    true))

(defn nil-if-empty [coll]
  (if (empty? coll)
    nil
    coll))

(defn input->cleaned-data [statement-type-config input-path]
  (let [cell-data-extraction-result (extract-cell-data-lines input-path)
        is-failure (= :error (:status cell-data-extraction-result))
        maybe-cell-data-lines (:cell-data-lines cell-data-extraction-result)]
    (cond
      is-failure
      (println "Unable to extract data from input file. Error: " (:error-message cell-data-extraction-result))

      (nil? (validate-cell-data-lines statement-type-config maybe-cell-data-lines))
      (println "Unable to validate the input file. Are you sure the statement type matches the file you are trying to process?")

      :default
      (->> maybe-cell-data-lines
           (remove-non-transaction-data statement-type-config)
           (transaction-data-lines->maps statement-type-config)
           nil-if-empty)
      )))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [[statement-type-user-input input-path output-path] args
        config-path (get-config-path)

        _
        (check-preconditions-with-exit-on-error config-path statement-type-user-input input-path output-path)

        config (load-config config-path)
        statement-type-config-key (statement-type->config-key statement-type-user-input)
        statement-type-config-raw (statement-type-config-key config)
        ; The statement type config key is used downstream to select how data will be extracted from the cell data lines.
        statement-type-config (assoc statement-type-config-raw :statement-type statement-type-config-key)]
    (if-let [cleaned-data (input->cleaned-data statement-type-config input-path)]
      (write-cleaned-data output-path cleaned-data)
      (println "No transactions could be parsed from the input file."))
    (shutdown-agents)))
