# Cordova ML Kit Plugin

Implements ML Kit as Cordova plugin on iOS and Android.

# Requirements

Mabs 6

only tested in cordova 7.1.0

## Installation

``cordova plugin add cordova-ml-kit-plugin ` `

## Features

| ML Kit Feature          | Android | Android (Cloud) | iOS | iOS (Cloud) |
|-------------------------|---------|-----------------|-----|-------------|
| Text recognition        | [ ]     | [ ]             | [ ] | [ ]         |
| Face detection          | [ ]     |                 | [ ] |             |
| Barcode scanning        | [ ]     |                 | [ ] |             |
| Image labeling          | [ ]     | [ ]             | [ ] | [ ]         |
| Landmark recognition    |         | [ ]             |     | [ ]         |
| Language identification | [ ]     |                 | [ ] |             |
| Translation             |         |                 |     |             |
| Smart Reply             | [ ]     |                 | [ ] |             |
| Custom model inference  | [ ]     |                 | [ ] |             |

| Feature                 | Android | iOS |
|-------------------------|---------|-----|
| Support Video Feed      | [ ]     | [ ] |

Some features of ML Kit are only available on device others only on cloud. Please see https://firebase.google.com/docs/ml-kit/ for more information!

## API Methods