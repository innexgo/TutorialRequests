import React from "react";
import { EventContentArg } from "@fullcalendar/react"
import format from 'date-fns/format';
import {sleep} from '../utils/utils'

import ReviewApptRequestModal from "../components/ReviewApptRequestModal";

function ApptRequestCard(props: { apptRequest: ApptRequest, apiKey: ApiKey }) {
  const apptRequest = props.apptRequest;

  const [reviewApptRequestModelShow, setReviewApptRequestModelShow] = React.useState(false);

  return (
    <div
      className="px-1 py-1 h-100 w-100 bg-danger text-dark overflow-auto"
      onClick={(e) => {
          setReviewApptRequestModelShow(true);
          e.stopPropagation();
      }}
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
      <ReviewApptRequestModal
        show={reviewApptRequestModelShow}
        setShow={(a:boolean) => {
          setReviewApptRequestModelShow(a);
          console.log(reviewApptRequestModelShow);
        }}
        apptRequest={apptRequest}
        apiKey={props.apiKey}
      />
    </div>
  )
}


function ApptCard(props: { appt: Appt, apiKey: ApiKey }) {
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

function AttendanceCard(props: { attendance: Attendance, apiKey: ApiKey }) {
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
