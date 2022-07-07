(in-ns 'ynab-importer-for-uae-banks.core)

(require '[clojure.string :as string])

(import java.time.format.DateTimeFormatter
        java.time.LocalDate
        java.util.Locale
        java.text.NumberFormat)

(def date-formatter (DateTimeFormatter/ofPattern "dd LLL yyyy"))
(defn parse-date [date-string]
  (LocalDate/parse date-string date-formatter))

(def number-formatter (NumberFormat/getInstance (Locale. "en" "ae")))
(defn parse-amount [amount-string]
  (.parse number-formatter amount-string))
(defn parse-amount-with-AED-symbol [amount-string]
  (parse-amount (string/replace-first amount-string "AED " "")))

(defn enbd-debit-transaction-lines->map [transaction-lines]
  (let [transactions (partition 4 transaction-lines)]
    (map (fn [[date description amount account-balance]]
           {:date        (parse-date date)
            :description description
            :amount      (parse-amount amount)})
         transactions)))

(defn enbd-credit-transaction-lines->map [transaction-lines]
  (let [transactions (partition 5 transaction-lines)]
    (map (fn [[transaction-date posting-date description card-type amount]]
           {:date        (parse-date transaction-date)
            :description description
            :amount      (parse-amount-with-AED-symbol amount)})
         transactions)))

(defn transaction-data-lines->maps [statement-type-config transaction-data-lines]
  (case (:statement-type statement-type-config)
    :enbd-debit (enbd-debit-transaction-lines->map transaction-data-lines)
    :enbd-credit (enbd-credit-transaction-lines->map transaction-data-lines)))