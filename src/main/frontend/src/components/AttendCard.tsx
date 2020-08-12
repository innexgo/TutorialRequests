import React from 'react';
import {Card, Button } from 'react-bootstrap';

type AttendCardProps = {
  student: string,
  time?: string,
}

export default function AttendCard({ student, time}: AttendCardProps){
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
return(
<Card style={cardStyle}>
    <Card.Body style={bodyStyle}>
      {student} - {time}
      <Button style={acceptStyle} variant="success">Present</Button>
      <Button style={rejectStyle} variant="warning">Tardy</Button>
      <Button style={rejectStyle} variant="danger">Absent</Button>
    </Card.Body>
  </Card>
);
}





