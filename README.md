# ynab-importer-for-uae-banks

A CLI app that converts my Excel based banking statements into a CSV that can be imported into the "You Need A Budget" budgeting app. Right now it can convert the following bank statement types:
- Emirates NBD
  - Current account statement
  - Credit card statement

Look at the [design document](design.md) for some details on why I needed this.

## Installation

The app is distributed as a single binary. You can find the latest version on the [releases page](https://github.com/theonejb/ynab-importer-for-uae-banks/releases). You should put it somewhere that's on your `$PATH` and mark it as executable via:

    chmod u+x ynab-importer-for-uae-banks

I've used the [lein binplus](https://github.com/BrunoBonacci/lein-binplus) plugin to generate an executable JAR file. As I understand, the executable part comes from a Bash shell script that is concatenated to the beginning of the JAR archive. Which means that the binary should run on most recent Linux or MacOS systems.

If you need to run on a different OS, you can download the Clojure code from this repo and compile it with [leiningen](http://leiningen.org).

## Usage

```shell
$ ynab-importer-for-uae-banks "Statement Type" "Input File Name" "Output File Name"
```

Statement type is one of the following:
- `ENBD-Debit`
- `ENBD-Credit`

You also need to provide a config file before you can use the app. This file should live at `~/.config/ynab-importer-for-uae-banks.edn` and have this format:

```clojure
{
 :enbd-credit {
               :validation-lines ["JIBRAN", "40000000001234"]
               }

 :enbd-debit  {
               :validation-lines ["LINE 1" "LINE 2" "LINE N"]
               }
}
```

The `:validation-lines` are consecutive cell values that must be present in the input file for each of the statement types. We use these to gain confidence that we are processing the right input file for the statement type you select.

Look at [the configuration section in the design document](design.md#config-file) for more details.

## Examples

    ynab-importer-for-uae-banks ENBD-Debit '/Users/asadjb/Downloads/Bank Imports/June 19/Credit_Statements.xml' out_debit.csv
    ynab-importer-for-uae-banks ENBD-Credit '/Users/asadjb/Downloads/Bank Imports/June 19/Credit_Statements.xml' out_credit.csv

## Contributing and future development plans
I've kept a log of my thoughts and actions in the [worklog](worklog.md). That file also lists some ideas I have for future development. I have one anti-goal for this app:
1. Don't overcomplicate it. I don't want this to be a general purpose app to transform statement from any bank to YNAB importable CSVs. This app is limited to supporting UAE bank statements only. This anti-goal should help in keeping this simple and easy to develop on.

If you would like to contribute new features, I'd suggest opening a Github issue and discussing it first. You're welcome to just start writing code and opening a pull request, but I can't guarantee that a PR will be accepted if it doesn't align with the goals and anti-goals.

## License
See [license](LICENSE).

Copyright © 2022 Asad Jibran Ahmed
