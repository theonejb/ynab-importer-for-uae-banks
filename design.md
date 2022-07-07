# YNAB importer for UAE Banks

# Context
This is a Clojure CLI program that I use to convert Excel files containing my bank statements into a format easily importable into You Need A Budget; a budgeting app.

# Problem
One of my banks, Emirates NBD (ENBD) exports statements in an old Excel XML format. The format of the spreadsheet is also not importable into YNAB.

Everytime I want to import transactions, I have to spend ~5 minutes dealing with the conversion. Beyond the time however, it's a painful thing to do and automating it will be delightful.

I also want to practice my Clojure skills.

# Interface
```shell
$ java -jar ynab-importer.jar "Statement Type" "Input File Name" "Output File Name"
```

Statement type is one of the following:
- ENBD-Debit
- ENBD-Credit

Input and output file names can be absolute or relative to the current directory.

## Config file
In order to run, you need to configure, for each statement type, a list of consecutive cell entries that can be used to validate the input file.

To extract data from the Excel files, this program uses the xml2 program:
```shell
$ xml2 < Credit_Statements.xls | grep '/Workbook/Worksheet/ss:Table/Row/Cell/Data='

/Workbook/Worksheet/ss:Table/Row/Cell/Data=ToDate
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Emirates NBD
/Workbook/Worksheet/ss:Table/Row/Cell/Data=My Finances
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Cards
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Credit Card Details
/Workbook/Worksheet/ss:Table/Row/Cell/Data=19 Jun 2022 03:39:01 PM
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Credit Card Statement
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Current Statement
/Workbook/Worksheet/ss:Table/Row/Cell/Data=U BY EMAAR VISA SIGNATURE
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Available Credit
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Minimum Payment
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Payment Due Date
/Workbook/Worksheet/ss:Table/Row/Cell/Data=
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Present Balance
/Workbook/Worksheet/ss:Table/Row/Cell/Data=
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Points Earned this Month
/Workbook/Worksheet/ss:Table/Row/Cell/Data=AED XX,XXX.XX
/Workbook/Worksheet/ss:Table/Row/Cell/Data=0.00 AED
/Workbook/Worksheet/ss:Table/Row/Cell/Data=21/06/2022
/Workbook/Worksheet/ss:Table/Row/Cell/Data=AED XX,XXX.XX
/Workbook/Worksheet/ss:Table/Row/Cell/Data=XXX
/Workbook/Worksheet/ss:Table/Row/Cell/Data=JIBRAN
/Workbook/Worksheet/ss:Table/Row/Cell/Data=****************
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Transaction Date
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Posting Date
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Description
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Card Type
/Workbook/Worksheet/ss:Table/Row/Cell/Data=Amount
/Workbook/Worksheet/ss:Table/Row/Cell/Data=18 Jun 2022
/Workbook/Worksheet/ss:Table/Row/Cell/Data=18 Jun 2022
/Workbook/Worksheet/ss:Table/Row/Cell/Data=<Description>
/Workbook/Worksheet/ss:Table/Row/Cell/Data=PRIMARY
/Workbook/Worksheet/ss:Table/Row/Cell/Data=AED XX,XXX.XX
/Workbook/Worksheet/ss:Table/Row/Cell/Data=16 Jun 2022
/Workbook/Worksheet/ss:Table/Row/Cell/Data=18 Jun 2022
/Workbook/Worksheet/ss:Table/Row/Cell/Data=SUBLIME HQ PTY LTD DOUBLE BAY AUS
/Workbook/Worksheet/ss:Table/Row/Cell/Data=PRIMARY
/Workbook/Worksheet/ss:Table/Row/Cell/Data=AED -XX,XXX.XX
```

The output of that is a list of data values. Each cell is on it's own line, so a single transaction row is spread over multiple lines. The example above is from my ENBD credit card statement.

To ensure that we don't process the wrong file for a statement type, we use a list of cell values that must be present in the file before it's accepted as valid for a statement type.

In the sample data above, there are two adjacent cells with values:
```
JIBRAN
****************
```

In the real data, the 2nd line is a partially masked credit card number. So for the `ENBD-Credit` statement type, I can configure the validation list to be:
```clojure
["JIBRAN", "40000000001234"]  ; Not my/a real card number :)
```

These configurations need to be present in a `.edn` config file in this format.

```clojure
{
 :enbd-credit {
               :validation-lines ["JIBRAN", "40000000001234"]
               }

 :enbd-debit {
              :validation-lines ["LINE 1" "LINE 2" "LINE N"]
              }
 }
```

The config file should live at `~/.config/ynab-importer-for-uae-banks.edn`.

_P.S: Did you notice me flexing my Sublime Text purchase from June in the sample statement above? :)_