# CIT Club Connect

CIT Club Connect is a native Android app for students to scan club activity fast, open richer event details, and jump straight to registration links. Clubs manage their own posts inside the app through a shared club login, so routine event updates never require code changes.

## What this build now includes

- A dark, gradient-led campus feed with compact event cards
- Club-specific banner colors (RGB + presets) chosen during club setup
- Club logos, club names, event titles, and short feed descriptions
- Event detail views with poster space and a register button
- Register-time opt-in reminders scheduled for 1 hour before event start
- Local image picking for club logos and event posters, uploaded to Supabase Storage
- Automatic cleanup for expired club posts and their Supabase-hosted poster images
- Supabase-backed club sign-in using a club ID plus password
- First-time club setup for club name, logo URL, contact person, contact email, and club description
- Club-owned event create, edit, feature, and delete actions
- Supabase Realtime for live event refresh while the app is open
- WorkManager + local notifications for reminder-style updates
- A separate `admin_app/` Android module for club/admin-only publishing workflows
- CIT Club Connect branding and a launcher/splash vector based on the provided logo

## Stack

- Kotlin + Jetpack Compose
- Supabase Auth
- Supabase Postgres
- Supabase Realtime
- Supabase Storage
- WorkManager

## Project structure

- `app/` member-facing Android application (feed + registration/reminders)
- `admin_app/` admin-only Android application (club sign-in + posting console)
- `supabase/schema.sql` SQL bootstrap for club profiles, events, row-level security, and realtime

## Supabase setup

1. Create a project in [Supabase](https://supabase.com/dashboard).
2. Open `Project Settings -> API`.
3. Copy:
   - `Project URL`
   - `anon public` key
4. Paste them into `gradle.properties`:

```properties
supabaseUrl=https://your-project-ref.supabase.co
supabaseAnonKey=your-anon-key
```

5. Open `SQL Editor`.
6. Run `supabase/schema.sql`.
7. Open `Authentication -> Providers -> Email`.
8. Enable Email auth and disable email confirmation if you want club setup to finish instantly inside the app.

If you are upgrading an existing deployment, run the updated `supabase/schema.sql` again so the new reminder and banner-color columns/policies are added.

Refresh behavior in this build:
- Foreground feed polling: every 15 seconds
- Background sync/notifications: WorkManager one-time chain every 5 minutes (best effort; Android may defer under battery/doze rules)

The SQL also creates a public Storage bucket called `club-media` with image-only uploads capped at 3 MB, which is a safer fit for the Supabase free tier.

## How club accounts work

- Clubs sign in with a `club ID` and `password`.
- Under the hood, the app looks up the club email saved during first setup, then uses that real email with Supabase Auth.
- The first time a club uses the app, they can create that shared club access and fill their public profile.
- After setup, the same club ID + password can be reused by that club to manage its own events.

Important:

- Keep the `club ID` stable after creating the access. It doubles as the login handle.
- Use a real club/contact email during first setup because Supabase Auth requires a valid email behind the scenes.
- The profile editor intentionally locks the club ID after setup so future sign-ins stay consistent.

## Tables created by the SQL

- `public.club_profiles`
  - One row per club owner auth account
  - Stores `club_id`, `club_name`, `club_logo_url`, contact info, and description
- `public.events`
  - Stores event details plus denormalized club identity data for fast feed rendering

The SQL also enables row-level security so a signed-in club can only change its own profile and events.

## Event publishing flow

1. A club signs in or completes first-time setup.
2. The club posts an event from the in-app club console.
3. Members instantly see the event in the compact home feed.
4. Tapping an event opens poster space, full details, and the registration button.
5. Background reminders keep members nudged about updates and upcoming events.

## Logo and splash

The app branding has already been updated to `CIT Club Connect`, and the current vector launcher/splash art is based on the provided `club-connect-icon.svg`.

If you want a pixel-perfect launcher icon generated from the exact SVG for all densities, you can still import the original artwork into Android Studio’s Image Asset tool later.

## Notes

- The feed trims long summaries to roughly 50 characters with `.....` so multiple events stay visible on screen.
- Clubs can upload logos and posters directly from the device. The app compresses them before upload to help stay within the free-tier storage budget.
- Expired events are automatically cleaned up, and uploaded poster files for those posts are deleted too when the app runs on a signed-in club device.
- Clubs can still leave poster URLs or registration links blank, and the URL field remains available as a fallback.
- The app can open before Supabase is configured, but live data and club publishing stay disabled until the keys are added.
- I could not run a full Android build in this environment unless Java/Gradle/Android SDK are available locally.
