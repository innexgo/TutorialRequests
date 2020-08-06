import React from 'react'
import { Form, Button } from 'react-bootstrap';
import DashboardLayout from '../../components/DashboardLayout';
import CSS from 'csstype';

export default function MakeAppt(props: AuthenticatedComponentProps) {
  const formStyle: CSS.Properties = {
    padding: '0% 3%',
  }
  const headerStyle: CSS.Properties = {
    marginTop: '2%',
    textAlign: 'center',
  }
  const buttonStyle: CSS.Properties = {
    marginTop: '2%',
  }
  return (
    <DashboardLayout {...props} >
      <h1 style={headerStyle}>Make an Appointment</h1>
      <Form style={formStyle}>
        <Form.Group controlId="date">
          <Form.Label>Date</Form.Label>
          <Form.Control type="date" />
        </Form.Group>

        <Form.Group controlId="teacher">
          <Form.Label>Student</Form.Label>
          <Form.Control as="select">
            <option>Marek</option>
            <option>Richard</option>
            <option>Govind</option>
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
