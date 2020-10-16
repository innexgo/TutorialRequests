import React from "react";
import { EventContentArg } from "@fullcalendar/react"
import { Popover, Card, Modal, Button, Form } from 'react-bootstrap';
import format from 'date-fns/format';

import UtilityWrapper from "../components/UtilityWrapper";

function ApptCard(appt: Appt) {
  return <UtilityWrapper title={appt.apptRequest.attendee.name} className="h-100 w-100 bg-white text-dark">
    <Popover id="information-tooltip">
      You have made an appointment with this student.
    </Popover>
    <div>
      <Card.Text>
        {format(appt.startTime, "hh:mm a")} -
        {format(appt.startTime + appt.duration, "hh:mm a")}
      </Card.Text>
    </div>
  </UtilityWrapper>
}

function ApptRequestCard(apptRequest: ApptRequest) {
  const style = {
    height: "100%",
    width: "100%",
    background: "red",
  };

  return <div>
    <h1>ApptRequest w/ {apptRequest.attendee.name}</h1>
  </div>

}

function AttendanceCard(attendance: Attendance) {
  return <div>
    <h1>Attendance w/ {attendance.appt.apptRequest.attendee.name}</h1>
  </div>
}


function UserCalendarCard(eventInfo: EventContentArg) {
  const props = eventInfo.event.extendedProps;
  switch (props.kind) {
    case "ApptRequest":
      return <ApptRequestCard {...props.apptRequest} />
    case "Appt":
      return <ApptCard {...props.appt} />
    case "Attendance":
      return <AttendanceCard {...props.attendance} />
  }
}

export default UserCalendarCard;
