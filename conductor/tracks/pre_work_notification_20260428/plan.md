# Implementation Plan - Pre-work Notifications

Add local notifications that remind users about their upcoming work shift. This includes settings to enable/disable the feature and choose how many minutes before the shift starts the notification should appear.

## Objective
- Provide a way for users to receive notifications before their work day starts.
- Add settings in the app to customize this behavior.

## Key Files & Context
- `app/src/main/res/values/strings.xml`: Notification labels and descriptions.
- `app/src/main/java/com/cirin0/worktimetracker/data/UserPreferencesRepository.kt`: Store notification settings.
- `app/src/main/java/com/cirin0/worktimetracker/features/settings/SettingsViewModel.kt`: Logic for managing notification settings.
- `app/src/main/java/com/cirin0/worktimetracker/features/settings/SettingsScreen.kt`: UI for enabling/disabling and configuring the notification.
- `app/src/main/java/com/cirin0/worktimetracker/core/notifications/PreWorkNotificationWorker.kt`: (New) WorkManager worker to trigger the notification.
- `app/src/main/java/com/cirin0/worktimetracker/core/notifications/WorkNotificationScheduler.kt`: (New) Utility to schedule/cancel the WorkManager tasks based on the work schedule.

## Implementation Steps

### 1. Resource Updates
- Add Ukrainian strings to `app/src/main/res/values/strings.xml`:
    - `settings_notifications`: "Сповіщення"
    - `settings_notification_before_work`: "Сповіщення перед роботою"
    - `settings_notification_before_work_desc`: "Нагадування про початок робочого дня"
    - `settings_notification_time`: "Час до початку"
    - `settings_notification_time_mins`: "%d хв"
- Add English strings to `app/src/main/res/values-en/strings.xml`.

### 2. Preferences Storage
- Update `UserPreferencesRepository.kt`:
    - Add `PRE_WORK_NOTIFICATION_ENABLED` (Boolean, default `false`).
    - Add `PRE_WORK_NOTIFICATION_MINUTES` (Int, default `15`).
    - Add flows and update methods for these keys.

### 3. Notification Worker
- Create `PreWorkNotificationWorker.kt`:
    - Uses `NotificationManager` to show a high-importance notification.
    - Title: "Час збиратися до роботи!" (or similar).
    - Body: "Ваш робочий день починається о [startTime]".

### 4. Scheduler Utility
- Create `WorkNotificationScheduler.kt`:
    - Method `scheduleNotification(context, schedule, leadMinutes)`:
        - Calculates the next work day's start time.
        - Calculates the delay from `now()`.
        - Enqueues a `OneTimeWorkRequest` with the calculated initial delay.
    - Method `cancelNotification(context)`:
        - Cancels any existing pre-work notification work.

### 5. ViewModel & UI
- Update `SettingsViewModel.kt`:
    - Expose `preWorkNotificationEnabled` and `preWorkNotificationMinutes` flows.
    - Add methods to update these values.
    - Trigger `WorkNotificationScheduler` when values change.
- Update `SettingsScreen.kt`:
    - Add a new "Сповіщення" section.
    - Add a switch for "Сповіщення перед роботою".
    - Add a dropdown for "Час до початку" (e.g., 5, 10, 15, 30, 60 mins).

### 6. Background Sync
- Ensure the notification is rescheduled when the user fetches their work schedule (e.g., in `WorkScheduleViewModel` or `HomeViewModel`).

## Verification & Testing
- **UI**: Verify the new settings appear in the Settings screen and correctly reflect the saved state.
- **Preferences**: Verify changing settings updates the DataStore.
- **Notification**: Set a test schedule, enable the notification for 1 minute before, and verify the notification appears correctly.
- **Cancellation**: Disable the notification and verify no future notifications are triggered.
