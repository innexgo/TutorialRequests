import React from 'react'
import { Row, Col, Modal, Form } from 'react-bootstrap';
import format from 'date-fns/format';

type ViewApptModalProps = {
  show: boolean;
  setShow: (show: boolean) => void;
  appt: Appt;
}

function ViewApptModal(props: ViewApptModalProps) {
  return <Modal
    className="ViewApptModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">View Appt Request</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Form>
        <Form.Group as={Row} controlId="startTime">
          <Form.Label column sm={2}>Start Time</Form.Label>
          <Col>
            <span>{format(props.appt.startTime, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="endTime">
          <Form.Label column sm={2}>End Time</Form.Label>
          <Col>
            <span>{format(props.appt.startTime+ props.appt.startTime, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="attendee">
          <Form.Label column sm={2}>Attendee</Form.Label>
          <Col>
            <span>{props.appt.apptRequest.attendee.name}</span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="host">
          <Form.Label column sm={2}>Host</Form.Label>
          <Col>
            <span>{props.appt.apptRequest.host.name}</span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="message">
          <Form.Label column sm={2}>Message</Form.Label>
          <Col>
            <span>{props.appt.message}</span>
          </Col>
        </Form.Group>
      </Form>
    </Modal.Body>
  </Modal>
}

export default ViewApptModal;
