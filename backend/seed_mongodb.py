import json
from pathlib import Path

from pymongo import MongoClient


ROOT = Path(__file__).resolve().parents[1]
ASSET_FILE = ROOT / "app" / "src" / "main" / "assets" / "students_2025.json"
MONGO_URI = "mongodb://127.0.0.1:27017"
DB_NAME = "iti_attendance"


def generated_student_email(name: str) -> str:
    local = "".join(ch.lower() for ch in name if ch.isalnum())
    return f"{local}@ITI.com"


def generated_student_password(dob: str) -> str:
    return "".join(ch for ch in dob if ch.isdigit())


def main():
    client = MongoClient(MONGO_URI)
    db = client[DB_NAME]

    students = json.loads(ASSET_FILE.read_text(encoding="utf-8"))
    for index, student in enumerate(students):
        student["studentId"] = f"student-{student.get('registrationNumber') or index}"
        student["loginEmail"] = generated_student_email(student.get("name", "student"))
        student["loginPassword"] = generated_student_password(student.get("dob", ""))

    trades = sorted({student["trade"] for student in students if student.get("trade")})
    teachers = [
        {
            "teacherId": "teacher-1",
            "name": "Anita Sharma",
            "email": "anita-sharma@ITI.com",
            "password": "teach@123",
            "trades": trades,
        },
        {
            "teacherId": "teacher-2",
            "name": "Rahul Verma",
            "email": "rahul-verma@ITI.com",
            "password": "welder@123",
            "trades": trades,
        },
    ]
    exams = [
        {"examId": "exam-1", "trade": "Electrician", "subject": "Trade Theory", "date": "2026-04-05", "time": "10:00", "room": "Lab 2"},
        {"examId": "exam-2", "trade": "Fitter", "subject": "Practical Viva", "date": "2026-04-08", "time": "11:30", "room": "Workshop"},
        {"examId": "exam-3", "trade": "Welder", "subject": "Safety Assessment", "date": "2026-04-10", "time": "09:30", "room": "Room 5"},
        {"examId": "exam-4", "trade": "Architectural Draughtsman (NSQF)", "subject": "Trade Drawing", "date": "2026-04-12", "time": "09:00", "room": "Studio 1"},
    ]

    db.students.delete_many({})
    db.teachers.delete_many({})
    db.exams.delete_many({})
    db.attendance.delete_many({})

    if students:
        db.students.insert_many(students)
    if teachers:
        db.teachers.insert_many(teachers)
    if exams:
        db.exams.insert_many(exams)

    print(f"Seeded {len(students)} students, {len(teachers)} teachers, {len(exams)} exams into {DB_NAME}")


if __name__ == "__main__":
    main()
