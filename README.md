# Receipts Go

> Save receipts, track expenses, and generate reports quickly and easily - all on your Android device. Receipts Go is an Open Source project.
> 
> [![Get it on Google Play](https://receiptsgo.app/wp-content/uploads/2024/12/google_play_get_small.png)](https://play.google.com/store/apps/details?id=com.wops.receiptsgo)

![ReceiptsGo](https://github.com/wirelessops/ReceiptsGo/blob/1f3d2aac10d5e595d393e7ccaf0ab7f342deab5d/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)



Turn your phone into a receipt scanner and expense report generator with Receipts Go! Track your receipts and easily generate beautiful PDF and CSV reports for yourself or your clients.

## Table of Contents

  - [Features](#features)
  - [Install](#install)
  - [Contribute](#contribute)
  - [License](#license)
  - [Attribution](#attribution)

## Features

- [X] Create expense report "folders" to categorise your receipts
- [X] Take receipt photos with your camera's phone
- [X] Import existing pictures on your device
- [X] Import PDF receipts 
- [X] Save receipt price, tax, and currency
- [X] Tag receipt names, categories, payment method, comments, and other metadata
- [X] Create/edit/delete all receipt categories
- [X] Track distance traveled for mileage reimbursement
- [X] Smart prediction based on past receipts
- [X] Generate PDF, CSV, & ZIP reports
- [X] Fully customizable report output
- [X] Graphical breakdowns of spending per category


## Install 

Receipts Go is broken into a few core modules:

- **app**. All common application code for both the `free` and `plusFlavor` flavors of the application are defined here. In practice, all development work should occur in this module
- **wBMiniLibrary**. A few legacy items that haven't been moved into the Library module, but it's otherwise unused.

Please note that that in order to use this project fully, you will need to replace the following place holder files:

- `app/src/main/res/values/secrets.xml`. You can copy the secrets.xml.sample file and rename the keys inside to achieve this behavior. This is used for low usage keys
- `app/src/free/res/values/ads.xml`. The ads file in smartReceiptsFree. You can add `adUnitId` and `classicAdUnitId` to enable support for AdMob Native and Classic Ads, respectively.
- `app/src/free/res/xml/analytics.xml`. The analytics file in smartReceiptsFree. You can add a key here if you wish to enable Google Analytics.

When running a build locally, we run the following operations via our gradle script to extract these secrets (if you have a valid GPG key) to allow the app to operate as expected:

- `git update-index --assume-unchanged app/src/free/res/values/ads.xml`
- `git update-index --assume-unchanged app/src/free/res/xml/analytics.xml`
- `git update-index --assume-unchanged app/src/main/res/values/secrets.xml`
- `gpg -d secrets.tar.gpg | tar xv`

The last command in this script uses a GPG key to extract the encrypted secrets from this file. Feel free to replace this key with your own local variant for testing/build purposes.


## Contribute

Contributions are always welcome! Please [open an issue](https://github.com/wirelessops/ReceiptsGo/issues/new) to report a bug or file a feature request to get started.

## Legacy Branch Access

We changed our way of saving secrets files starting with `release_4.14.0`. If you wish to interact with a release prior to then, you should perform the following:

1. `tar cvf secrets.tar app/src/main/res/values/secrets.xml app/src/free/res/values/ads.xml app/src/free/res/xml/analytics.xml`  
2. `rm app/src/free/res/values/ads.xml`
3. `rm app/src/free/res/xml/analytics.xml` 
4. `rm app/src/main/res/values/secrets.xml`
5. `git checkout YOUR_DESIRED_BRANCH`
6. `tar xvf secrets.tar`

Which should allow these to build properly with the placeholder files.

## License

```none
The GNU Affero General Public License (AGPL)

Copyright (c) 2012-2018 Smart Receipts LLC (Will Baumann)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

## Attribution

```none
Google Play and the Google Play logo are trademarks of Google LLC.
```
