import React from "react";
import FullCalendar, { EventContentArg, EventChangeArg } from "@fullcalendar/react"
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import { Row, Col, Card, Modal, Button, Form } from 'react-bootstrap';
import format from 'date-fns/format';

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
