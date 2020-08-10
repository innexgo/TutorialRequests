import React from 'react';
import { Card, Col, Row, Button, Form } from 'react-bootstrap';

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
      <Col style={{margin: '1rem'}}>
        <Row style={{fontSize: '2rem'}}>
          {student} - {date}
        </Row>
        <Row>
          <Form.Group controlId="message">
            <Form.Label>Student's Comment</Form.Label>
            <Form.Control as="textarea" rows={9} readOnly />
          </Form.Group>
        </Row>
      </Col>
      <Col style={{margin: '1rem', marginTop: '4rem'}}>
        <Row>
          <Form.Group controlId="time">
            <Form.Label>Start Time</Form.Label>
            <Form.Control type="time" />
          </Form.Group>
          <Form.Group controlId="time">
            <Form.Label>End Time</Form.Label>
            <Form.Control type="time" />
          </Form.Group>
          <Form.Group controlId="message">
            <Form.Label>Teacher's Comment</Form.Label>
            <Form.Control as="textarea" rows={3} />
          </Form.Group>
        </Row>
      </Col>
        <Button style={acceptStyle} variant="success">Accept</Button>
        <Button style={rejectStyle} variant="danger">Reject</Button>
    </Card.Body>
  </Card>
);
}
