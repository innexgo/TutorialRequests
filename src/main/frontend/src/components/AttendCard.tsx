import React from 'react';
import {Card, Button } from 'react-bootstrap';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

type AttendCardProps = {
  student: string,
  apptId: number,
  time: string,
  apiKey: ApiKey,
}

export default function AttendCard({ student, apptId, time, apiKey}: AttendCardProps){
const cardStyle = {
  backgroundColor: '#4472C4',
  margin: '0 2%',
  borderRadius: '10px',
}
const bodyStyle = {
  color: 'white',
  display: 'flex',
}
const acceptStyle = {
  marginLeft: 'auto',
}
const rejectStyle = {
  marginLeft: '1%',
}

async function present() {
  const appt = await fetchApi(`apptRequest/setAttendance/?` + new URLSearchParams([
    ['apptRequestId', `${apptId}`],
    ['attendanceStatus', "present"],
    ['apiKey', apiKey.key],
    ]
    )) as ApptRequest;
  }
async function tardy() {
  const appt = await fetchApi(`apptRequest/setAttendance/?` + new URLSearchParams([
    ['apptRequestId', `${apptId}`],
    ['attendanceStatus', "tardy"],
    ['apiKey', apiKey.key],
    ]
    )) as ApptRequest;
  }
async function absent() {
  const appt = await fetchApi(`apptRequest/setAttendance/?` + new URLSearchParams([
    ['apptRequestId', `${apptId}`],
    ['attendanceStatus', "absent"],
    ['apiKey', apiKey.key],
  ]
  )) as ApptRequest;
  }


return(
<Card style={cardStyle}>
    <Card.Body style={bodyStyle}>
      {student} - {time}
      <Button style={acceptStyle} variant="success" onClick={async () => present()}>Present</Button>
      <Button style={rejectStyle} variant="warning" onClick={async () => tardy()}>Tardy</Button>
      <Button style={rejectStyle} variant="danger"  onClick={async () => absent()}>Absent</Button>
    </Card.Body>
  </Card>
);
}





