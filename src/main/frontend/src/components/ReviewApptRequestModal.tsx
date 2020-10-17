import React from "react";
import FullCalendar, { EventContentArg, EventChangeArg } from "@fullcalendar/react"
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import { Row, Col, Card, Modal, Button, Form } from 'react-bootstrap';
import format from 'date-fns/format';

import { fetchApi } from '../utils/utils';


type ReviewApptRequestModal = {
  show: boolean;
  setShow: (show: boolean) => void;
  apptRequest: ApptRequest;
  apiKey: ApiKey;
}

function ReviewApptRequestModal(props: ReviewApptRequestModal) {
  const [message, setMessage] = React.useState("");
  const [duration, setDuration] = React.useState(props.apptRequest.duration);
  const [startTime, setStartTime] = React.useState(props.apptRequest.startTime);

  const ignore = () => props.setShow(false)
  const accept = async () => {
    await fetchApi('appt/new/?' + new URLSearchParams([
      ["apptRequestId", `${props.apptRequest.apptRequestId}`],
      ["message", message],
      ["startTime", `${startTime}`],
      ["duration", `${duration}`],
      ["apiKey", `${props.apiKey.key}`]
    ]));
    props.setShow(false);
  }

  const eventChangeHandler = (eca:EventChangeArg) => {
    const start =eca.event.start!.valueOf();
    const end = eca.event.end!.valueOf();
    setStartTime(start);
    setDuration(end-start);
  }

  return <Modal
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Review Student Request</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Row>
        <Col>
          <Form>
            <Form.Group as={Row} controlId="startTime">
              <Form.Label column sm={4}>Start Time</Form.Label>
              <Col>
              <Form.Control readOnly plaintext defaultValue={format(startTime, "MMM do, hh:mm a")} />
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="endTime">
              <Form.Label column sm={4}>End Time</Form.Label>
              <Col>
              <Form.Control readOnly plaintext defaultValue={format(startTime + duration, "MMM do, hh:mm a")} />
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="student">
              <Form.Label column sm={4}>Student</Form.Label>
            </Form.Group>
            <Form.Group as={Row} controlId="message">
              <Form.Label column sm={4}>Message</Form.Label>
              <Col>
                <Form.Control as="textarea" rows={3}
                  onChange={e => {
                    setMessage(e.target.value);
                  }} />
              </Col>
            </Form.Group>
            <Form.Group controlId="submit">
              <Button variant="success" onClick={accept}>
                Accept
              </Button>
              <Button variant="danger" onClick={ignore}>
                Ignore
              </Button>
            </Form.Group>
          </Form>
        </Col>
        <Col>
          <FullCalendar
            plugins={[timeGridPlugin, interactionPlugin]}
            initialView="timeGridDay"
            headerToolbar={false}
            allDaySlot={false}
            slotMinTime="08:00"
            slotMaxTime="18:00"
            eventChange={eventChangeHandler}
            initialDate={props.apptRequest.startTime}
            height="auto"
            events={[{
              title: "Appointment Time (Drag to change)",
              start: startTime,
              end: startTime + duration,
              editable: true
            }]}
          />
        </Col>
      </Row>
    </Modal.Body>
  </Modal>
}

export default ReviewApptRequestModal;
