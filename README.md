# Eurovision Score Tracker

An Android app for scoring Eurovision entries in real time with a group, from the same couch or remotely. Scores sync between all devices. 

**[→ App landing page](https://github-0.github.io/est)**



## Download

Get the latest APK from [Releases](../../releases). 


## Build from source

You need Android Studio and your own Firebase project with Firestore and Anonymous Auth enabled.

1. Clone the repo
2. Place your `google-services.json` in `app/` (see `app/google-services.json.template`)
3. Build and install

### Backend setup

- Deploy the included security rules to Firebase
- Shows and official results are uploaded to Firestore with `python3 "maintenance tools/admin.py"`, which needs your Firebase service account key in `maintenance tools/service_account.json` (see the `.template` next to it). A show can only be selected once its participants have been uploaded.

### Before you ship your own build

This code is written for the original app and its shared backend. Some things are specific to it and may need changing in your build. 


## License

[MIT](LICENSE). The project is published as-is and does not accept contributions or issues.
