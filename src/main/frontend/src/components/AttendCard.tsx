import React from 'react';
import { Card, Button } from 'react-bootstrap';
import { fetchApi } from '../utils/utils';

type AttendCardProps = {
  student: string,
  apptId: number,
  time: string,
  apiKey: ApiKey,
}

export default function AttendCard({ student, apptId, time, apiKey }: AttendCardProps) {
  async function submitAttendance(kind:AttendanceKind) {
    const attendance = await fetchApi(`attendance/new/?` + new URLSearchParams([
      ['apptId', `${apptId}`],
      ['kind', kind],
      ['apiKey', apiKey.key],
    ])) as Attendance;
  }

  return (
    <Card >
      <Card.Body >
        {student} - {time}
        <Button variant="success" onClick={async () => submitAttendance("PRESENT")}>Present</Button>
        <Button variant="warning" onClick={async () => submitAttendance("TARDY")}>Tardy</Button>
        <Button variant="danger" onClick={async () => submitAttendance("ABSENT")}>Absent</Button>
      </Card.Body>
    </Card>
  );
}
