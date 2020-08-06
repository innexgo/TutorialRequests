import React from 'react';
import {Card, Button } from 'react-bootstrap';

type ApptCardProps = {
  student: string,
  date: string,
}

export default function ApptCard({ student, date }: ApptCardProps){
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
      {student} - {date}
      <Button style={acceptStyle} variant="success">Accept</Button>
      <Button style={rejectStyle} variant="danger">Reject</Button>
    </Card.Body>
  </Card>
);
}





