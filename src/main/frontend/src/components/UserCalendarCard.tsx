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

type ApptRequestCardProps = {
  apptRequest: ApptRequest,
  apiKey: ApiKey
}

function ApptRequestCard(props: ApptRequestCardProps) {
  const apptRequest = props.apptRequest;

  const [show, setShow] = React.useState(false);

  return (
    <div>
      <div
        onClick={() => setShow(true)}
        className="px-1 py-1 h-100 w-100 bg-danger text-dark overflow-auto"
      >
        <span>
          {format(apptRequest.startTime, "h:mm a")} - {format(apptRequest.startTime + apptRequest.duration, "h:mm a")}
        </span>
        <br />
        <span>
          Request From: {apptRequest.attendee.name}
        </span>
        <br />
        <span>
          Msg: {apptRequest.message}
        </span>
      </div>
      <ReviewApptRequestModal
        show={show}
        setShow={setShow}
        apptRequest={apptRequest}
        apiKey={props.apiKey}
      />
    </div >
  )
}


type ApptCardProps = {
  appt: Appt,
  apiKey: ApiKey
}

function ApptCard(props: ApptCardProps) {
  const appt = props.appt;
  const [show, setShow] = React.useState(false);

  return <div className="px-1 py-1 h-100 w-100 bg-warning text-dark overflow-auto">
    <span>
      {format(appt.startTime, "h:mm a")} - {format(appt.startTime + appt.duration, "h:mm a")}
    </span>
    <br />
    <span>
      Appt: {appt.apptRequest.attendee.name}
    </span>
    <br />
    <span>
      Msg: {appt.message}
    </span>
  </div>
}

type AttendanceCardProps = {
  attendance: Attendance,
  apiKey: ApiKey
}

function AttendanceCard(props: AttendanceCardProps) {
  const attendance = props.attendance;
  return <div className="px-1 py-1 h-100 w-100 bg-success text-dark overflow-auto">
    {attendance.kind}
    <br />
    {format(attendance.appt.startTime, "h:mm a")} - {format(attendance.appt.startTime + attendance.appt.duration, "h:mm a")}
    <br />
    Appt: {attendance.appt.apptRequest.attendee.name}
  </div>
}


function UserCalendarCard(eventInfo: EventContentArg) {
  const props = eventInfo.event.extendedProps;
  switch (props.kind) {
    case "ApptRequest":
      return <ApptRequestCard apptRequest={props.apptRequest} apiKey={props.apiKey} />
    case "Appt":
      return <ApptCard appt={props.appt} apiKey={props.apiKey} />
    case "Attendance":
      return <AttendanceCard attendance={props.attendance} apiKey={props.apiKey} />
  }
}

export default UserCalendarCard;
