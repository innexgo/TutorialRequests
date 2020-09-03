import React from 'react'
import { Form, Button } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import { fetchApi } from '../utils/utils';
import moment from 'moment';


export default function UserApptCreator(props: AuthenticatedComponentProps) {
  const formStyle = {
    padding: '0% 3%',
  }
  const headerStyle = {
    marginTop: '2%',
    textAlign: 'center' as const,
  }
  const buttonStyle = {
    marginTop: '2%',
  }

  const [date, setDate] = React.useState("");
  const [startTime, setStartTime] = React.useState("");
  const [endTime, setEndTime] = React.useState("");
  const [student, setStudent] = React.useState("");
  const [message, setMessage] = React.useState("");

  async function createAppt(){
    const start = moment(date + " " + startTime, "YYYY-M-D H:mm").valueOf();
    const end = moment(date + " " + endTime, "YYYY-M-D H:mm").valueOf();
    const duration = end-start;
    const appt = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['userId', `${props.apiKey.user.id}`],
      ['studentId', `${student}`],
      ['message', message],
      ['requestTime', `${start}`],
      ['requestDuration', `${duration}`],
      ['approved', 'true'],
      ['reviewed', 'true'],
      ['apiKey', `${props.apiKey.key}`],
  ])) as ApptRequest;
  }

  return (
  <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
      <h1 style={headerStyle}>Make an Appointment</h1>

      
      <Form style={formStyle}>
        <Form.Group controlId="date">
          <Form.Label>Date</Form.Label>
          <Form.Control type="date"
            onChange={e => {
              setDate(e.target.value);
              }} />
        </Form.Group>
        <Form.Group controlId="startTime">
          <Form.Label>Start Time</Form.Label>
          <Form.Control type="time" 
            onChange={e => {
              setStartTime(e.target.value);
            }} />
        </Form.Group>
        <Form.Group controlId="endTime">
          <Form.Label>End Time</Form.Label>
          <Form.Control type="time" 
            onChange={e => {
              setEndTime(e.target.value);
              }} />
        </Form.Group>

        <Form.Group controlId="student">
          <Form.Label>Student ID</Form.Label>
          <Form.Control as="textarea" rows={1} 
            onChange={e => {
              setStudent(e.target.value);
            }} />
        </Form.Group>

        <Form.Group controlId="message">
          <Form.Label>Message</Form.Label>
          <Form.Control as="textarea" rows={3} 
            onChange={e => {
              setMessage(e.target.value);
         }} />
        </Form.Group>

        <Button style={buttonStyle} variant="primary" type="submit" onClick={async () => createAppt()}>Submit</Button>
      </Form>
    </DashboardLayout>
  );
}

