import React from 'react'
import SearchUserDropdown from '../components/SearchUserDropdown';

import { Row, Col, Modal, Button, Form } from 'react-bootstrap';
import { fetchApi } from '../utils/utils';
import format from 'date-fns/format';

type CreateApptModalProps = {
  show: boolean;
  start: number;
  duration: number;
  setShow: (show: boolean) => void;
  apiKey: ApiKey;
}

function CreateApptModal(props: CreateApptModalProps) {
  const [studentId, setStudentId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  const submit = async () => {
    const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['targetId', `${studentId}`],
      ["attending", 'false'],
      ['message', message],
      ['startTime', `${props.start}`],
      ['duration', `${props.duration}`],
      ['apiKey', `${props.apiKey.key}`],
    ])) as ApptRequest;

    await fetchApi('appt/new/?' + new URLSearchParams([
      ["apptRequestId", `${apptRequest.apptRequestId}`],
      ["message", message],
      ["startTime", `${props.start}`],
      ["duration", `${props.duration}`],
      ["apiKey", `${props.apiKey.key}`]
    ]));
    props.setShow(false);
  }

  return <Modal
    className="CreateApptModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Create Appointment with Student</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Form>
        <Form.Group as={Row}>
          <Form.Label column sm={2}>Start Time</Form.Label>
          <Col>
            <span>{format(props.start, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row}>
          <Form.Label column sm={2}>End Time</Form.Label>
          <Col>
            <span>{format(props.start + props.duration, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row}>
          <Form.Label column sm={2}>Student ID</Form.Label>
          <Col>
            <SearchUserDropdown apiKey={props.apiKey} userKind={"STUDENT"} setFn={e => setStudentId(e)} />
          </Col>
        </Form.Group>
        <Form.Group as={Row}>
          <Form.Label column sm={2}>Message</Form.Label>
          <Col>
            <Form.Control as="textarea" rows={3}
              onChange={e => {
                setMessage(e.target.value);
              }} />
          </Col>
        </Form.Group>
        <Button variant="primary" disabled={studentId == null} onClick={submit}>
          Submit
        </Button>
      </Form>
    </Modal.Body>
  </Modal>
}

export default CreateApptModal;
