# License Admin

A tiny private app - only for you. Lets you see every customer who has opened the
Desktop POS software, and flip a switch to instantly block or restore their access.

## How it connects

It talks to the same Firebase project as everything else. The Desktop app writes a
`licenses/{uid}` document the first time each customer logs in (always starts
**enabled**). This app lists all of those documents and lets you flip `enabled`
off/on. The Desktop app is watching its own license document live — the moment you
switch someone off, if they're already using the software it signs them out within
a second or two; if they try to log in while off, they're blocked immediately.

## REQUIRED setup before building

You must set your own admin email in **two places**, or this won't work:

1. `app/src/main/java/com/talha/licenseadmin/LicenseAdminApp.kt` — change
   `ADMIN_EMAIL` to your Firebase account's email.
2. `RestaurantPOS/firestore.rules` — change the email inside `isSuperAdmin()` to the
   **same** email, then paste the updated rules into Firebase Console → Firestore
   Database → Rules → Publish (same steps as before).

Only that one email will ever be able to open this app or toggle anyone's access —
if the two don't match exactly, you'll get "permission denied" when it tries to
list customers.

## Building (same as the Rider app)

Upload this folder to its own new GitHub repository, same as `rider-app` and
`RestaurantDesktop`. GitHub Actions builds the APK automatically — download it from
the repo's **Actions** tab → latest run → **Artifacts** →
`license-admin-debug-apk`.

## Using it

1. Install the APK, open the app, log in with your admin email/password.
2. You'll see every restaurant that has ever opened the Desktop app, with a switch.
3. Turn a switch off → that restaurant can no longer log in to the Desktop
   software, and gets logged out within seconds if they're already in it.
4. Turn it back on → they can log in normally again.

Keep this APK private — don't share it with customers, since it controls
everyone's access.
