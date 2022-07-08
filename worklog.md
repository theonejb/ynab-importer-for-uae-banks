A log of work that I've done so that I remember where to pick up from when coming back to it later.

- Trying out https://github.com/mjul/docjure to see if it can read the wierd Excel format ENBD exports.
- The sheet that ENBD exports is not an .xls as the extension would suggest. It's an older format called SpreadSheetML that is no longer maintained and is not something that Apache POI (backing library of docjure) can read.
- I will try to read it as an XML and see if that works.
  - If not, I can use regex. :oh-god:
- Instead of putting myself through the hell of XML parsing, I'm using the neat `xml2` utility from the brew xml2 package. It outputs the monstrosity of the exported XML file as a field per line. Example below of my credit card statement file.

```
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

- I can use this to extract just the relevant fields, then join them into vectors of 5 items, each representing a row of data. Might be brittle, but seems to work fine for now. Will probably need some validation to check the conversion happens peacefully.
- It's similar for the debit account file, but with a slightly different transaction row format.
- `partition` with a step size of 1 can give a sliding window. I can use that to find the starting position of the list. I can then partition the rest
- Config file with a vector of strings to match to validate the file is a correct input. For instance the card #, etc.
- Config file made and format added to design.md
- CLI input handling done. Next is to run the shell command to read the input file.
- File input done. Filtering also done. Next is file validation.
- MVP is ready :yay:
  - Validated against a manual export by using a simple Python shell session. All transactions are output correctly to a YNAB importable CSV.

- Next idea is to build a more complicated system around this that can ease/automate the payee matching.
  - For now, the payee is the raw description from my statement, which looks like `POS-PURCHASE CARD NO.**************** ***** 01-06-2022  5.00,USD NOTION LABS, INC. HTTPSWWW.NOTI:US`.
  - YNAB can't (as far as I can tell) match this to the Notion payee that's already in their DB, probably because they do a full match against the description.
  - I'd like a situation where I can specify pairs of (regex, output string w/ matched groups) to clean up the descriptions. So for the above, I could have a pair like:
    - `('^POS-PURCHASE CARD NO .* NOTION LABS, INC\. HTTPSWWW\.NOTI:US$', 'Notion')`
    - This would convert the description into "Notion" in the output, allowing YNAB to easily match the payee as well.
  - I'd like the script to output the replacements it does, grouped by the regex each description matches. This can allow me to validate that the correct transactions match.
  - I'd like the script to also ask for replacements for descriptions it could not automatically replace.
  - Eventually I'd like the script to store any new replacements I make into a DB, and to run the whole process again if I add a new regex.

- For now, the immediate next step is to work on releasing this. A GitHub repo with a nicely formatted readme. Then share it widely.
- Would also like to add an option `--filter after=2022-01-01` to only output transactions that happen after this date.
- Using https://github.com/BrunoBonacci/lein-binplus for packaging. This generates a bin file I can upload for Unix like systems.
- Need to use clojure cli package to provide better CLI args support.