# Privacy Policy

**Effective date:** September 10, 2026

Couple Moments ("the app") is developed and maintained by Pedro Vicente ("we", "us"). This policy
explains what data the app does and does not handle. We built Couple Moments to work entirely
on your device, so there isn't much to explain — but here's the full picture.

## Summary

- No account or sign-up, ever.
- No personal data is collected, transmitted, or shared with us or anyone else.
- No analytics, crash reporting, advertising, or third-party SDKs of any kind.
- All app data (which cards you've hidden, your language choice, whether you've unlocked the
  Intimacy category) is stored locally on your device and never leaves it.
- The only network activity the app can perform is an optional check for app updates, and only in
  the version of the app distributed via GitHub Releases (see [Network access](#network-access)
  below). The version distributed through the Play Store makes no network calls at all.

## Data we collect

None. Couple Moments has no backend, no server, and no account system. We do not receive, store,
or have access to any information about you or your use of the app.

## Data stored on your device

The app stores the following locally, using Android's standard app storage (a local database and
app preferences). This data is never transmitted anywhere:

- **Card state** — which conversation-starter cards you've swiped away/hidden, so they stay out of
  rotation until you reset them in Settings.
- **Language preference** — your selected in-app display language.
- **Intimacy category preference** — whether you've chosen to unlock the Intimacy category of
  cards.

Uninstalling the app permanently deletes all of this data. There is no cloud backup or sync tied to
your identity; if your device uses Android's own system backup (`android:allowBackup`), that backup
is created and controlled by Android itself and stored wherever you've configured your device
backups to go (e.g., your own Google account) — we have no access to it.

## Network access

Couple Moments is distributed in two forms, which behave differently:

- **Play Store version** — makes no network requests whatsoever. All permissions related to
  networking or package installation are absent from this build.
- **GitHub Releases version** (direct APK download) — includes an optional, user-initiated
  "Update to latest" feature in Settings. When you tap it, the app queries this project's public
  GitHub Releases API to check for a newer version and, if you confirm, downloads and installs the
  update APK. This request does not include any personal information — it is the same kind of
  anonymous request your browser would make to view the page. No other network activity occurs in
  either build.

## Permissions

- `INTERNET` — GitHub Releases build only, used solely for the update check described above.
- `REQUEST_INSTALL_PACKAGES` — GitHub Releases build only, used solely to let you install an update
  APK you've explicitly chosen to download. Granting it does not allow the app to install anything
  without your action.

Neither permission is present in the Play Store build.

## Children's privacy

Couple Moments is intended for adult couples and includes a category of romantic/sexual
conversation prompts (Intimacy), which is opt-in and hidden by default. The app is not directed at
children, and since we don't collect any data from anyone, we don't knowingly collect data from
children either.

## Changes to this policy

If this policy changes, we'll update the effective date above and the revision history in this
file's version control. Continued use of the app after a change constitutes acceptance of the
updated policy.

## Contact

Questions about this policy or the app can be sent to
[play-store-neteinstein@googlegroups.com](mailto:play-store-neteinstein@googlegroups.com), or filed as an issue on the
[project's GitHub repository](https://github.com/neteinstein/CoupleMoments).
