

## What This App Is

Android Studio app in Kotlin + Jetpack Compose for ITI student and teacher attendance tracking.

## Current Status

- Student login is supported.
- Teacher login is supported.
- Student dashboard includes:
  - attendance overview
  - vertical class schedule
  - lectures tab with subject-wise attendance details
  - exam schedule
- Teacher dashboard includes:
  - mark attendance
  - view attendance
  - student details
  - exam schedule
- The UI was reworked to be more polished and mobile friendly.
- Pixel 9 text contrast issue was fixed by forcing the light theme.
- Teacher dashboard scrolling was changed to a proper scrollable layout.

## Login Rules

### Student

- Username format: `studentname@ITI.com`
- Example: `aayanshaikh@ITI.com`
- Password format: DOB as `DDMMYYYY`
- Example: `01082008`

Student login generation is handled in:

- `app/src/main/java/com/iti/edutrack/data/DemoSchoolRepository.kt`
- `app/src/main/java/com/iti/edutrack/data/StudentAssetLoader.kt`

### Teacher

Sample teacher login:

- `anita-sharma@ITI.com`
- `teach@123`

## Real Student Data

The app now uses the imported Excel student list bundled as an asset:

- Source Excel used: `D:\student info 2025 (1).xls`
- Bundled app asset: `app/src/main/assets/students_2025.json`

Imported records: 1129 students.

The asset is loaded at startup from:

- `app/src/main/java/com/iti/edutrack/MainActivity.kt`
- `app/src/main/java/com/iti/edutrack/data/StudentAssetLoader.kt`

## Important Files

- App entry: `app/src/main/java/com/iti/edutrack/MainActivity.kt`
- Repository: `app/src/main/java/com/iti/edutrack/data/DemoSchoolRepository.kt`
- Asset loader: `app/src/main/java/com/iti/edutrack/data/StudentAssetLoader.kt`
- Models: `app/src/main/java/com/iti/edutrack/data/model/Models.kt`
- Login UI: `app/src/main/java/com/iti/edutrack/ui/screens/LoginScreen.kt`
- Student UI: `app/src/main/java/com/iti/edutrack/ui/screens/StudentScreens.kt`
- Teacher UI: `app/src/main/java/com/iti/edutrack/ui/screens/TeacherScreens.kt`
- Theme: `app/src/main/java/com/iti/edutrack/ui/theme/Theme.kt`
- Database diagram: `docs/database-schema.md`

## Known Limitation

The lecture-wise attendance details shown in the student dashboard are currently generated sample attendance tied to imported students.

Reason:

- The Excel sheet contains student profile data, not full historical lecture attendance records.

So:

- student names, trades, DOB, caste, etc. are real from Excel
- lecture attendance detail rows are currently generated demo data

## Next Best Step

Connect the app to a real backend / MongoDB so:

- students are stored in MongoDB
- teacher-marked attendance is persisted
- lecture-wise attendance becomes real data instead of generated data
- future Excel imports can upsert students automatically

## Build Check

Last verified successfully with:

- `./gradlew testDebugUnitTest`

## Notes For Next Session

- If text looks washed out on device, keep the app on the forced light theme.
- If teacher trade selection looks incomplete, use `repository.availableTrades()` not teacher-seeded subsets.
- If login examples need changing, update them in `LoginScreen.kt`.
