from datetime import datetime
from typing import Optional

from fastapi import FastAPI, Query
from pydantic import BaseModel
from pymongo import MongoClient


MONGO_URI = "mongodb://127.0.0.1:27017"
DB_NAME = "iti_attendance"

client = MongoClient(MONGO_URI)
db = client[DB_NAME]

app = FastAPI(title="ITI Attendance Backend")


class AttendancePayload(BaseModel):
    trade: str
    attendanceDate: str
    markedByTeacherEmail: str
    records: list[dict]


@app.get("/health")
def health():
    return {"status": "ok", "database": DB_NAME}


@app.get("/trades")
def trades():
    values = sorted(db.students.distinct("trade"))
    return {"trades": values}


@app.get("/students")
def students(trade: Optional[str] = Query(default=None)):
    query = {}
    if trade:
        query["trade"] = trade
    docs = list(
        db.students.find(
            query,
            {"_id": 0}
        ).sort("name", 1)
    )
    return {"students": docs}


@app.get("/exams")
def exams(trade: Optional[str] = Query(default=None)):
    query = {}
    if trade:
        query["trade"] = trade
    docs = list(db.exams.find(query, {"_id": 0}).sort("date", 1))
    return {"exams": docs}


@app.get("/attendance")
def attendance(trade: str, attendance_date: str):
    docs = list(
        db.attendance.find(
            {"trade": trade, "attendanceDate": attendance_date},
            {"_id": 0}
        ).sort("studentName", 1)
    )
    return {"attendance": docs}


@app.post("/attendance")
def mark_attendance(payload: AttendancePayload):
    db.attendance.delete_many(
        {"trade": payload.trade, "attendanceDate": payload.attendanceDate}
    )
    documents = [
        {
            "trade": payload.trade,
            "attendanceDate": payload.attendanceDate,
            "markedByTeacherEmail": payload.markedByTeacherEmail,
            "studentId": record["studentId"],
            "studentName": record["studentName"],
            "registrationNumber": record["registrationNumber"],
            "isPresent": bool(record["isPresent"]),
            "createdAt": datetime.utcnow().isoformat(),
        }
        for record in payload.records
    ]
    if documents:
        db.attendance.insert_many(documents)
    return {"inserted": len(documents)}
