# MongoDB Compass Setup

## What Was Added

A local MongoDB flow was added for this project:

- Mongo startup script: `scripts/start_local_mongodb.ps1`
- Seed/import script: `scripts/seed_local_mongodb.ps1`
- Local backend: `backend/app.py`
- Mongo seed logic: `backend/seed_mongodb.py`

## Local MongoDB Connection

Use this connection string in MongoDB Compass:

`mongodb://127.0.0.1:27017`

Database name:

`iti_attendance`

Collections:

- `students`
- `teachers`
- `attendance`
- `exams`

## How To Show Data In Compass

1. Start MongoDB:

```powershell
.\scripts\start_local_mongodb.ps1
```

2. Seed the database:

```powershell
.\scripts\seed_local_mongodb.ps1
```

3. Open MongoDB Compass.
4. Connect to:

```text
mongodb://127.0.0.1:27017
```

5. Open database `iti_attendance`.

## What Gets Seeded

- 1129 students from `app/src/main/assets/students_2025.json`
- teacher sample accounts
- sample exam schedule

Each student document also gets:

- `loginEmail`
- `loginPassword`

based on:

- username: `studentname@ITI.com`
- password: DOB as `DDMMYYYY`

## Optional Backend

To run the local API:

```powershell
python -m pip install -r .\backend\requirements.txt
uvicorn backend.app:app --reload
```

Then open:

- `http://127.0.0.1:8000/health`
- `http://127.0.0.1:8000/trades`
- `http://127.0.0.1:8000/students`

## Important Note

The Android app UI still uses its in-app repository flow right now.
This MongoDB backend and Compass setup gives you a real database path and real seeded collections.
If you want the Android app itself to read/write through this backend next, the next step is wiring the app screens to these API endpoints.
