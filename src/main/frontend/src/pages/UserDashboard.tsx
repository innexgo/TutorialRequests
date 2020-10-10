import React, { useState } from 'react'
import FullCalendar, { EventInput, EventClickArg, DateSelectArg } from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import UserDashboardLayout from '../components/UserDashboardLayout';
import SearchUserDropdown from '../components/SearchUserDropdown';

import { Popover, Container, CardDeck, Modal, Button, Form } from 'react-bootstrap';
import Utility from '../components/Utility';
import { fetchApi } from '../utils/utils';

interface ApptProps {
  appointments: Appt[],
  apiKey: ApiKey
}
function EventCalendar(props: ApptProps) {

  const [show, setShow] = useState(false);
  const [date, setDate] = useState("");
  const handleClose = () => setShow(false);


  const [startTime, setStartTime] = React.useState("");
  const [endTime, setEndTime] = React.useState("");
  const [studentId, setStudentId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  async function createAppt() {
    const start = new Date(0).valueOf();
    const end = new Date(0).valueOf();
    const duration = end - start;
    const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['attending', 'false'],
      ['targetId', `${studentId!}`],
      ['message', message],
      ['suggestedTime', `${start}`],
      ['apiKey', `${props.apiKey.key}`],
    ])) as ApptRequest;

    const appt = await fetchApi('appt/new/?' + new URLSearchParams([
      ["apptRequestId", `${apptRequest.apptRequestId}`],
      ["hostId", `${props.apiKey.creator.id}`],
      ["attendeeId", `${studentId}`],
      ["message", message],
      ["startTime", `${start}`],
      ["duration", `${duration}`],
      ["apiKey", `${props.apiKey.key}`]
    ])) as Appt;
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


  const loadEvents = () =>
    props.appointments.map((x: Appt) => {
      console.log(x);
      return {
        id: `${x.apptRequest.apptRequestId}`,
        title: x.apptRequest.attendee.name,
        start: new Date(x.startTime),
        end: new Date(x.startTime + x.duration),
        allDay: false
      };
    });



  return (
      <FullCalendar
        plugins={[timeGridPlugin, interactionPlugin]}
        headerToolbar={{
          left: 'prev,next today',
          center: '',
          right: 'timeGridDay,timeGridWeek',
        }}
        initialView='timeGridWeek'
        allDaySlot={false}
        nowIndicator={true}
        editable={false}
        selectable={true}
        selectMirror={true}
        dayMaxEvents={true}
        businessHours={{
          daysOfWeek: [1, 2, 3, 4, 5], // MTWHF
          startTime: '08:00', // 8am
          endTime: '18:00' // 6pm
        }}
        select={handleDateSelect}
        events={loadEvents()}
      />)
   let foo =
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
              <SearchUserDropdown apiKey={props.apiKey} userKind={"STUDENT"} setFn={e => setStudentId(e)} />
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
}


function UserDashboard(props: AuthenticatedComponentProps) {
  const loadData = async (apiKey: ApiKey): Promise<ApptProps> => {
    const appointments = await fetchApi('appt/?' + new URLSearchParams([
      ['offset', '0'],
      ['count', `${0xFFFFFFFF}`],
      ['hostId', `${apiKey.creator.id}`],
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
            {data => <EventCalendar {...data} />}
          </Utility>
        </CardDeck>
      </Container>
    </UserDashboardLayout>
  )
};

export default UserDashboard;
