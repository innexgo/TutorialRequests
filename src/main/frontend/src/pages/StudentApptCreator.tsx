import React from 'react'
import { Form, Button } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';


function StudentApptCreator(props: StudentComponentProps) {
  const formStyle = {
    padding: '0% 3%',
  };

  const headerStyle = {
    marginTop: '2%',
    textAlign: 'center' as const,
  };

  const buttonStyle = {
    marginTop: '2%',
  };

  const [date, setDate] = React.useState("");
  const [teacher, setTeacher] = React.useState("");
  const [message, setMessage] = React.useState("");


  /*async function createAppt(){
    const start = moment(date, 'YYYY-M-D').valueOf();
    const appt = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
    //TODO dynamically generate dropdown with all users (teachers), with value as teacher Id and placeholder as teacher name. get teacher value and plug into userID.
      ['userId', ],
      ['studentId', props.student.id],
      ['message', message],
      ['requestTime', start],
      ['requestDuration', 0],
      ['approved', 'false'],
      ['reviewed', 'false'],
      ['apiKey', apiKey.key],
  ])) as ApptRequest;
  }*/


  return (
    <DashboardLayout name={props.student.name} logoutCallback={() => props.setStudent(null)} >
      <h1 style={headerStyle}>Make an Appointment</h1>
      <Form style={formStyle}>
        <Form.Group controlId="date">
          <Form.Label>Date</Form.Label>
          <Form.Control type="date" />
        </Form.Group>
        <Form.Group controlId="time">
          <Form.Label>Time</Form.Label>
          <Form.Control type="time" />
        </Form.Group>

        <Form.Group controlId="teacher">
          <Form.Label>Teacher</Form.Label>
          <Form.Control as="select">
            <option>Ms. Ng</option>
            <option>Ms. Cornejo</option>
            <option>Ms. Dimas</option>
          </Form.Control>
        </Form.Group>

        <Form.Group controlId="message">
          <Form.Label>Message</Form.Label>
          <Form.Control as="textarea" rows={3} />
        </Form.Group>

        <Button style={buttonStyle} variant="primary" type="submit">Submit</Button>
      </Form>
    </DashboardLayout>
  );
}


export default StudentApptCreator;
