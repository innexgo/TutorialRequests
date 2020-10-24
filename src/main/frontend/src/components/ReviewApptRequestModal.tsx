import React from "react";
import FullCalendar, { EventChangeArg, DateSelectArg } from "@fullcalendar/react"
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import { Row, Col, Modal, Button, Form } from 'react-bootstrap';
import format from 'date-fns/format';

import { fetchApi } from '../utils/utils';

type ReviewApptRequestModalProps = {
  show: boolean;
  setShow: (show: boolean) => void;
  apptRequest: ApptRequest;
  apiKey: ApiKey;
}

function ReviewApptRequestModal(props: ReviewApptRequestModalProps) {
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

  const eventChangeHandler = (eca: EventChangeArg) => {
    const start = eca.event.start!.valueOf();
    const end = eca.event.end!.valueOf();
    setStartTime(start);
    setDuration(end - start);
  }

  return <Modal
    className="ReviewApptRequestModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title>Review Student Request</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Row>
        <Col>
          <Form>
            <Form.Group as={Row}>
              <Form.Label column sm={4}>Start Time</Form.Label>
              <Col>
                <span >
                  {format(startTime, "MMM do, hh:mm a")}
                </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row}>
              <Form.Label column sm={4}>End Time</Form.Label>
              <Col>
                <span>
                  {format(startTime + duration, "MMM do, hh:mm a")}
                </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row}>
              <Form.Label column sm={4}>Student</Form.Label>
              <Col>
                <span  >
                  {props.apptRequest.attendee.name}
                </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row}>
              <Form.Label column sm={4}>Message</Form.Label>
              <Col>
                <Form.Control as="textarea" rows={3}
                  onChange={e => {
                    setMessage(e.target.value);
                  }} />
              </Col>
            </Form.Group>
            <Button variant="success" onClick={accept}>
              Accept
              </Button>
            <Button variant="danger" onClick={ignore}>
              Ignore
              </Button>
          </Form>
        </Col>
        <Col>
          <FullCalendar
            plugins={[timeGridPlugin, interactionPlugin]}
            initialView="timeGridDay"
            unselectCancel=".ReviewApptRequestModal"
            headerToolbar={false}
            allDaySlot={false}
            slotMinTime="08:00"
            slotMaxTime="18:00"
            eventChange={eventChangeHandler}
            selectable={true}
            selectMirror={true}
            initialDate={props.apptRequest.startTime}
            height="auto"
            events={[{
              start: props.apptRequest.startTime,
              end: props.apptRequest.startTime + props.apptRequest.duration,
              display: "background"
            }]}
            select={(dsa: DateSelectArg) => {
              setStartTime(dsa.start.valueOf());
              setDuration(dsa.end.valueOf() - dsa.start.valueOf());
            }}
            unselect={() => {
              setStartTime(props.apptRequest.startTime);
              setDuration(props.apptRequest.duration);
            }}
          />
        </Col>
      </Row>
    </Modal.Body>
  </Modal>
}

export default ReviewApptRequestModal;
