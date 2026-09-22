# Privacy Policy

**Effective date:** September 22, 2026

Couple Moments ("the app") is developed and maintained by Pedro Vicente ("we", "us"). This policy
explains what data the app does and does not handle. We built Couple Moments to work almost
entirely on your device, so there isn't much to explain — but here's the full picture.

## Summary

- No account or sign-up, ever.
- No advertising, no ad networks, no advertising identifiers, and nothing is ever sold or shared
  with a data broker.
- Your questions, answers and conversations are never recorded or transmitted. The app has no
  place to type anything in, and the card you are looking at is never sent anywhere.
- The Android and Web versions report **anonymous usage analytics** (Google Firebase Analytics) —
  which screens and features get used, and how often. You can turn this off at any time in
  **Settings → Privacy**. The iOS version reports nothing.
- All your app data (which cards you've hidden, your language choice, your theme, whether you've
  unlocked the Intimacy category) is stored locally on your device and never leaves it.

## Analytics

To understand which parts of the app are actually useful — which categories get browsed, whether
the Game tab gets opened, whether anyone changes the theme — the Android and Web versions send
anonymous usage events to Google Firebase Analytics.

**What is sent:**

- Which screen you are on (Questions, Game, Settings, Splash).
- Coarse interactions: a card was viewed, a card was hidden, a category filter was applied, the
  grid/deck view was toggled, the 18+ Intimacy notice was shown and accepted or dismissed, a
  setting was changed, hidden cards were reset, an update was checked for.
- Non-identifying context for those events: the category of card (e.g. "memories"), the content
  language, whether the "For Parents" set is on, your theme choice, and the platform.
- A random identifier generated on your device the first time the app runs. It is not your
  account, your email, your device id, or an advertising id, and it is not derived from any of
  them. Clearing the app's data or reinstalling produces a new one. It exists so that "1,000
  events" can be told apart from "one person opening the app 1,000 times".
- Whatever Firebase Analytics collects automatically for any app: approximate (city/country-level)
  location derived from IP address, device model, OS version, app version and session timing.
  Google does not retain the IP address itself in the analytics data.

**What is never sent:**

- The text of any question or card you look at.
- Anything you say to each other. The app has no microphone access, no text input, and no way to
  record a conversation.
- Your name, email address, contacts, photos, precise location, or advertising identifier. The
  advertising-id permissions that the analytics SDK normally requests are explicitly removed from
  this app's Android builds.

**Turning it off:** open **Settings → Privacy** and switch off "Share anonymous usage data". This
disables collection in the SDK itself — not just the events the app sends — so nothing further is
reported, including Firebase's own automatic events, and the random identifier above stops being
attached to anything. (It stays stored on your device, so that turning analytics back on later
counts you as the same person rather than a new one.) The choice is remembered on your device. On
the Web version, opting out also stops the analytics SDK from loading at all on subsequent page
loads.

Data collected this way is processed by Google as our data processor; see
[Google's Privacy Policy](https://policies.google.com/privacy) and
[How Google uses data from sites or apps that use our services](https://policies.google.com/technologies/partner-sites).

## Data we collect

Other than the anonymous analytics described above, none. Couple Moments has no backend of our
own, no server, and no account system. We cannot identify you, and we have no way to connect
anything in the analytics data to a real person.

## Data stored on your device

The app stores the following locally, using the platform's standard app storage (a local database
and app preferences; on the Web version, your browser's `localStorage`). This data is never
transmitted anywhere:

- **Card state** — which conversation-starter cards you've swiped away/hidden, so they stay out of
  rotation until you reset them in Settings.
- **Language preference** — your selected in-app display language.
- **Theme preference** — light, dark or follow-the-system.
- **Intimacy category preference** — whether you've chosen to unlock the Intimacy category of
  cards.
- **Analytics preference** — whether you've turned off "Share anonymous usage data", and the
  random analytics identifier described above.

Uninstalling the app permanently deletes all of this data. There is no cloud backup or sync tied to
your identity; if your device uses Android's own system backup (`android:allowBackup`), that backup
is created and controlled by Android itself and stored wherever you've configured your device
backups to go (e.g., your own Google account) — we have no access to it.

## Network access

Both Android builds send the anonymous analytics described above, unless you turn them off. Beyond
that, the two builds differ:

- **Play Store version** — makes no other network requests.
- **GitHub Releases version** (direct APK download) — additionally includes an optional,
  user-initiated "Update to latest" feature in Settings. When you tap it, the app queries this
  project's public GitHub Releases API to check for a newer version and, if you confirm, downloads
  and installs the update APK. This request does not include any personal information — it is the
  same kind of anonymous request your browser would make to view the page.

The Web version loads the Firebase Analytics library from Google's `gstatic.com` CDN, unless you
have opted out. The iOS version makes no network requests at all.

## Permissions

- `INTERNET` and `ACCESS_NETWORK_STATE` — both Android builds, used to send the anonymous analytics
  described above and (GitHub Releases build only) for the update check.
- `REQUEST_INSTALL_PACKAGES` — GitHub Releases build only, used solely to let you install an update
  APK you've explicitly chosen to download. Granting it does not allow the app to install anything
  without your action.

The advertising-identifier permissions (`AD_ID`, `ACCESS_ADSERVICES_AD_ID`,
`ACCESS_ADSERVICES_ATTRIBUTION`) that the Firebase Analytics SDK declares by default are explicitly
removed from both Android builds — this app has no ads and does no attribution.

## Children's privacy

Couple Moments is intended for adult couples and includes a category of romantic/sexual
conversation prompts (Intimacy), which is opt-in and hidden by default. The app is not directed at
children, and we do not knowingly collect data from children. The analytics described above are
anonymous and contain nothing that could identify a child (or anyone else).

## Changes to this policy

If this policy changes, we'll update the effective date above and the revision history in this
file's version control. Continued use of the app after a change constitutes acceptance of the
updated policy.

## Contact

Questions about this policy or the app can be sent to
[play-store-neteinstein@googlegroups.com](mailto:play-store-neteinstein@googlegroups.com), or filed as an issue on the
[project's GitHub repository](https://github.com/neteinstein/CoupleMoments).
