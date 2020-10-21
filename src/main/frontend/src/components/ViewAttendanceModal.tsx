import React from 'react'
import { Row, Col, Modal, Form } from 'react-bootstrap';
import format from 'date-fns/format';

type ViewAttendanceModalProps = {
  show: boolean;
  setShow: (show: boolean) => void;
  attendance: Attendance;
}

function ViewAttendanceModal(props: ViewAttendanceModalProps) {
  return <Modal
    className="ViewAttendanceModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">View Attendance</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Form>
        <Form.Group as={Row} controlId="attendanceKind">
          <Form.Label column sm={2}>Attendance</Form.Label>
          <Col>
            <span>{props.attendance.kind}</span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="startTime">
          <Form.Label column sm={2}>Start Time</Form.Label>
          <Col>
            <span>{format(props.attendance.appt.startTime, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="endTime">
          <Form.Label column sm={2}>End Time</Form.Label>
          <Col>
            <span>{format(props.attendance.appt.startTime+ props.attendance.appt.startTime, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="attendee">
          <Form.Label column sm={2}>Attendee</Form.Label>
          <Col>
            <span>{props.attendance.appt.apptRequest.attendee.name}</span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="host">
          <Form.Label column sm={2}>Host</Form.Label>
          <Col>
            <span>{props.attendance.appt.apptRequest.host.name}</span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="message">
          <Form.Label column sm={2}>Message</Form.Label>
          <Col>
            <span>{props.attendance.appt.message}</span>
          </Col>
        </Form.Group>
      </Form>
    </Modal.Body>
  </Modal>
}

export default ViewAttendanceModal;
