import React from "react"
import SearchUserDropdown from "../components/SearchUserDropdown";
import { Formik, FormikHelpers } from "formik";
import { Row, Col, Modal, Button, Form } from "react-bootstrap";
import { newApptRequest, isApiErrorCode } from "../utils/utils";
import format from "date-fns/format";

type CreateApptRequestModalProps = {
  show: boolean;
  start: number;
  duration: number;
  setShow: (show: boolean) => void;
  apiKey: ApiKey;
}

function CreateApptRequestModal(props: CreateApptRequestModalProps) {
  const [userId, setUserId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  const submit = async () => {
    await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ["targetId", `${userId}`],
      ["attending", "true"],
      ["message", message],
      ["startTime", `${props.start}`],
      ["duration", `${props.duration}`],
      ["apiKey", `${props.apiKey.key}`],
    ]));
    props.setShow(false);
  }

  return <Modal
    className="CreateApptRequestModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Request Appointment with Teacher</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Form>
        <Form.Group as={Row} controlId="startTime">
          <Form.Label column sm={2}>Start Time</Form.Label>
          <Col>
            <span>{format(props.start, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="endTime">
          <Form.Label column sm={2}>End Time</Form.Label>
          <Col>
            <span>{format(props.start + props.duration, "MMM do, hh:mm a")} </span>
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="userId">
          <Form.Label column sm={2}>Teacher ID</Form.Label>
          <Col>
            <SearchUserDropdown apiKey={props.apiKey} userKind={"USER"} setFn={e => setUserId(e)} />
          </Col>
        </Form.Group>
        <Form.Group as={Row} controlId="message">
          <Form.Label column sm={2}>Message</Form.Label>
          <Col>
            <Form.Control as="textarea" rows={3}
              onChange={e => {
                setMessage(e.target.value);
              }} />
          </Col>
        </Form.Group>
        <Button variant="primary" disabled={userId == null} onClick={submit}>
          Submit
        </Button>
      </Form>
    </Modal.Body>
  </Modal>
}

export default CreateApptRequestModal;
