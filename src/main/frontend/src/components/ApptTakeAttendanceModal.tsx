import React from 'react'
import SearchUserDropdown from '../components/SearchUserDropdown';

import { Row, Col, Modal, Button, Form } from 'react-bootstrap';
import { fetchApi } from '../utils/utils';
import format from 'date-fns/format';

type ApptTakeAttendanceModalProps = {
  show: boolean;
  setShow: (show: boolean) => void;
  appt: Appt;
  apiKey: ApiKey;
}

function ApptTakeAttendanceModal(props: ApptTakeAttendanceModalProps) {
  const [studentId, setStudentId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  async function submitAttendance(kind: AttendanceKind) {
    const attendance = await fetchApi(`attendance/new/?` + new URLSearchParams([
      ['apptId', `${props.appt.apptRequest.apptRequestId}`],
      ['attendanceKind', kind],
      ['apiKey', props.apiKey.key],
    ])) as Attendance;
    props.setShow(false);
  }

  const past = Date.now() > props.appt.startTime;

  return <Modal
    className="ApptTakeAttendanceModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Take Attendance</Modal.Title>
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
            <span>{format(props.appt.startTime + props.appt.duration, "MMM do, hh:mm a")}</span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="student">
          <Form.Label column sm={2}>Student</Form.Label>
          <Col>
            <span>{props.appt.apptRequest.attendee.name}</span>
          </Col>
        </Form.Group>
        {past ? <> </> :
          <Form.Group as={Row} controlId="status">
            <Form.Label column sm={2}>Status</Form.Label>
            <Col>
              <span>Future, can't take attendance yet</span>
            </Col>
          </Form.Group>
        }
        <Form.Group as={Row} controlId="attendance">
          <Form.Label column sm={2}>Take Attendance</Form.Label>
          <Col>
            <Button variant="success" disabled={!past} onClick={async () => await submitAttendance("PRESENT")}>
              Present
            </Button>
            <Button variant="warning" disabled={!past} onClick={async () => await submitAttendance("TARDY")}>
              Tardy
            </Button>
            <Button variant="danger" disabled={!past} onClick={async () => await submitAttendance("ABSENT")}>
              Absent
            </Button>
          </Col>
        </Form.Group>
      </Form>
    </Modal.Body>
  </Modal>
}

export default ApptTakeAttendanceModal;
