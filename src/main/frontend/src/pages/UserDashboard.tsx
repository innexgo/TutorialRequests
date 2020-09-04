import React, { useState } from 'react'
import FullCalendar, { EventInput, EventClickArg, DateSelectArg } from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import UserDashboardLayout from '../components/UserDashboardLayout';
import SearchUserDropdown from '../components/SearchUserDropdown';

import { Popover, Container, CardDeck, Modal, Button, Form } from 'react-bootstrap';
import Utility from '../components/Utility';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface ApptProps {
  appointments: Appt[],
  apiKey: ApiKey
}



function LoadEvents(props: ApptProps) {

  const [show, setShow] = useState(false);
  const [date, setDate] = useState("");
  const handleClose = () => setShow(false);


  const [startTime, setStartTime] = React.useState("");
  const [endTime, setEndTime] = React.useState("");
  const [studentId, setStudentId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  async function createAppt() {
    const start = moment(date + " " + startTime, "YYYY-M-D H:mm").valueOf();
    const end = moment(date + " " + endTime, "YYYY-M-D H:mm").valueOf();
    const duration = end - start;
    const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['targetId', `${studentId!}`],
      ['message', message],
      ['suggestedTime', `${start}`],
      ['apiKey', `${props.apiKey.key}`],
    ])) as ApptRequest;

    const appt = await fetchApi('appt/new/?' + new URLSearchParams([
      ["apptRequestId", `${apptRequest.id}`],
      ["hostId", `${props.apiKey.user.id}`],
      ["attendeeId", `${studentId}`],
      ["message", message],
      ["startTime", `${start}`],
      ["duration", `${duration}`],
      ["apiKey", `${props.apiKey.key}`]
    ])) as Appt;
  }

  const INITIAL_EVENTS: EventInput[] =
    props.appointments.map((x:Appt) => {
      console.log(x);
      return {
        id: `${x.id}`,
        title: `${x.attendee.name}`,
        start: `${moment(x.startTime).format("yyyy-mm-dd[T]h:mm:ss")}`,
        end: `${moment(x.startTime + x.duration).format("yyyy-mm-dd[T]h:mm:ss")}`,
        allDay: false
      }
    });

  const handleEventClick = (clickInfo: EventClickArg) => {
    if (window.confirm(`Are you sure you want to delete the appointment with'${clickInfo.event.title}'`)) {
      clickInfo.event.remove()
      //TODO NEED TO ACTUALLY DELETE EVENT FROm BACKEND
    }
  }

  const handleDateSelect = (selectInfo: DateSelectArg) => {

    let calendarApi = selectInfo.view.calendar

    calendarApi.unselect() // clear date selection

    setShow(true);
    setDate(selectInfo.startStr);

    /* calendarApi.addEvent({
       id: createEventId(),
       title,
       start: selectInfo.startStr,
       end: selectInfo.endStr,
       allDay: selectInfo.allDay
     })*/
    //TODO not sure if we need to add event through calendar api
  }


  return (
    <>
      <FullCalendar
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay'
        }}
        initialView='dayGridMonth'
        editable={false}
        selectable={true}
        selectMirror={true}
        dayMaxEvents={true}
        weekends={false}
        select={handleDateSelect}
        eventClick={handleEventClick}
        initialEvents={INITIAL_EVENTS}

      />
      <Modal
        show={show}
        onHide={handleClose}
        backdrop="static"
        keyboard={false}
        size="lg"
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title id="modal-title">Make Appointment</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group controlId="startTime">
              <Form.Label>Start Time</Form.Label>
              <Form.Control type="time"
                onChange={e => {
                  setStartTime(e.target.value);
                }} />
            </Form.Group>
            <Form.Group controlId="endTime">
              <Form.Label>End Time</Form.Label>
              <Form.Control type="time"
                onChange={e => {
                  setEndTime(e.target.value);
                }} />
            </Form.Group>

            <Form.Group controlId="student">
              <Form.Label>Student ID</Form.Label>
              <SearchUserDropdown apiKey={props.apiKey} userKind={"ADMIN"} setFn={e => setStudentId(e)} />
            </Form.Group>

            <Form.Group controlId="message">
              <Form.Label>Message</Form.Label>
              <Form.Control as="textarea" rows={3}
                onChange={e => {
                  setMessage(e.target.value);
                }} />
            </Form.Group>

            <Button variant="primary" onClick={async () => createAppt()}>Submit</Button>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>

            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}


function UserDashboard(props: AuthenticatedComponentProps) {
  const loadData = async (apiKey: ApiKey): Promise<ApptProps> => {
    const appointments = await fetchApi('appt/?' + new URLSearchParams([
      ['offset', '0'],
      ['count', `${0xFFFFFFFF}`],
      ['hostId', `${apiKey.user.id}`],
      ['apiKey', apiKey.key]
    ])) as Appt[];
    return {
      appointments,
      apiKey
    }
  };

  return (
    <UserDashboardLayout {...props} >
      <Container fluid className="py-3 px-3">
        <CardDeck>
          <Utility<ApptProps> title="Calendar" promise={loadData(props.apiKey)}>
            <Popover id="information-tooltip">
              This screen shows all future appointments. You can click any date to add an appointment on that date, or click an existing appointment to delete it.
           </Popover>
            {data => <LoadEvents {...data} />}
          </Utility>
        </CardDeck>
      </Container>
    </UserDashboardLayout>
  )
};

export default UserDashboard;
