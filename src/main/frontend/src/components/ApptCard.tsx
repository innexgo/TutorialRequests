import React from 'react';
import { Card, Col, Row, Button, Form } from 'react-bootstrap';
import { format } from 'date-fns';
import { fetchApi } from '../utils/utils';

//date pass date as milliseconds
type ApptCardProps = {
  student: string,
  time: number,
  duration: number,
  studentMessage: string,
  apptId: number,
  apiKey: ApiKey,
}

export default function ApptCard({ student, time, duration, studentMessage, apptId, apiKey }: ApptCardProps) {
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

  const [startTime, setStartTime] = React.useState("");
  const [endTime, setEndTime] = React.useState("");
  const [response, setResponse] = React.useState("");

  async function acceptAppt() {
    const start = Date.parse(startTime);
    const end = Date.parse(endTime);

    const appt = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['apptRequestId', `${apptId}`],
      ['approved', 'true'],
      ['response', response],
      ['requestTime', `${start}`],
      ['requestDuration', `${end - start}`],
      ['apiKey', apiKey.key],
    ]
    )) as ApptRequest;
  }

  async function rejectAppt() {
    const start = Date.parse(startTime);
    const end = Date.parse(endTime);

    const appt = await fetchApi('apptRequest/review/?' + new URLSearchParams([
      ['apptRequestId', `${apptId}`],
      ['approved', 'false'],
      ['response', response],
      ['requestTime', `${start}`],
      ['requestDuration', `${end - start}`],
      ['apiKey', apiKey.key],
    ]
    )) as ApptRequest;

  }

  return (
    <Card>
      <Card.Body style={bodyStyle}>
        <Col style={{ margin: '1rem' }}>
          <Row style={{ fontSize: '2rem' }}>
            {student} - {format(new Date(time), 'MM Do')}
          </Row>
          <Row>
            <Form.Group controlId="message">
              <Form.Label>Student's Comment</Form.Label>
              <Form.Control as="textarea" rows={9} readOnly defaultValue={studentMessage} />
            </Form.Group>
          </Row>
        </Col>
        <Col style={{ margin: '1rem', marginTop: '4rem' }}>
          <Row>
            <Form.Group controlId="time">
              <Form.Label>Start Time</Form.Label>
              <Form.Control type="time"
                onChange={e => {
                  setStartTime(e.target.value);
                }} />
            </Form.Group>
            <Form.Group controlId="time">
              <Form.Label>End Time</Form.Label>
              <Form.Control type="time"
                onChange={e => {
                  setEndTime(e.target.value);
                }} />
            </Form.Group>
            <Form.Group controlId="message">
              <Form.Label>Teacher's Comment</Form.Label>
              <Form.Control as="textarea" rows={3}
                onChange={e => {
                  setResponse(e.target.value);
                }} />
            </Form.Group>
          </Row>
        </Col>
        <Button style={acceptStyle} variant="success" onClick={async () => acceptAppt()}>Accept</Button>
        <Button style={rejectStyle} variant="danger" onClick={async () => rejectAppt()}>Reject</Button>
      </Card.Body>
    </Card>
  );
}
