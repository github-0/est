# Eurovision Score Tracker

An Android app for scoring Eurovision entries in real time with a group, from the same couch or remotely. Scores sync between all devices. 

**[→ App landing page](https://github-0.github.io/est)**


## Download

Get the latest APK from [Releases](../../releases). 


## Build from source


### Prerequisites

- Android Studio
- A Firebase project with Firestore and Anonymous Auth enabled

### Setup

1. Clone the repo
2. Place your `google-services.json` to app/google-services.json
3. Build and install

### Seeding participant data

The app needs Eurovision participant data uploaded to Firestore before a show is selectable. 

Place your Firebase service account key to `Maintenance tools/service_account.json`.

Add participant data to `Maintenance tools/participants.json`, then run:

```bash
python3 "Maintenance tools/admin.py"
```


### Firestore rules

Deploy the included security rules to your Firebase project:

```bash
firebase deploy --only firestore:rules
```
