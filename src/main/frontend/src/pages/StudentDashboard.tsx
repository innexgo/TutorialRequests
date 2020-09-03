import React, { useState } from 'react'
import FullCalendar, { EventInput, EventClickArg, DateSelectArg } from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import StudentDashboardLayout from '../components/StudentDashboardLayout';

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

  const [apptDate, setapptDate] = React.useState("");
  const [teacher, setTeacher] = React.useState("");
  const [message, setMessage] = React.useState("");


  /*async function createAppt(){
    const start = moment(date, 'YYYY-M-D').valueOf();
    const appt = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
    //TODO dynamically generate dropdown with all users (teachers), with value as teacher Id and placeholder as teacher name. get teacher value and plug into userID.
      ['studentId', `${props.apiKey.id}],
      ['message', message],
      ['requestTime', start],
      ['requestDuration', 0],
      ['approved', 'false'],
      ['reviewed', 'false'],
      ['apiKey', props.apiKey.key],
  ])) as ApptRequest;
  }*/

  const events = props.appointments;

  const INITIAL_EVENTS: EventInput[] =
    events.map((x) =>
      ({
        id: `${x.id}`,
        title: `${x.host.name}`,
        start: `${moment(x.startTime).format("yyyy-mm-dd[T]h:mm:ss")}`,
        end: `${moment(x.startTime + x.duration).format("yyyy-mm-dd[T]h:mm:ss")}`,
        allDay: false
      })
    );


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
        selectable={false}
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
            <Form.Group controlId="date">
              <Form.Label>Date</Form.Label>
              <Form.Control type="date" value={date}
                onChange={e => {
                  setapptDate(e.target.value);
                }}
              />
            </Form.Group>

            <Form.Group controlId="teacher">
              <Form.Label>Teacher</Form.Label>
              <Form.Control as="select"
                onChange={e => {
                  setTeacher(e.target.value);
                }} >
                <option>Ms. Ng</option>
                <option>Ms. Cornejo</option>
                <option>Ms. Dimas</option>

              </Form.Control>
            </Form.Group>

            <Form.Group controlId="message">
              <Form.Label>Message</Form.Label>
              <Form.Control as="textarea" rows={3}
                onChange={e => {
                  setMessage(e.target.value);
                }} />
            </Form.Group>

            <Button variant="primary" type="submit">Submit</Button>
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

function StudentCalendar(props: AuthenticatedComponentProps) {
  const loadData = async (apiKey: ApiKey): Promise<ApptProps> => {
    const appointments = await fetchApi('appt/?' + new URLSearchParams([
      ['offset', '0'],
      ['count', '0xFFFFFFFF'],
      ['user_id', `${apiKey.user.id}`],
      ['approved', 'true'],
      ['apiKey', apiKey.key]
    ])) as Appt[];
    return {
      appointments,
      apiKey
    }
  };

  return (
    <StudentDashboardLayout {...props} >
      <Container fluid className="py-3 px-3">
        <CardDeck>
          <Utility<ApptProps> title="Pending Appointments" promise={loadData(props.apiKey)} >
            <Popover id="information-tooltip">
              This screen shows all future appointments. You can click any date to add an
              appointment on that date, or click an existing appointment to delete it.
            </Popover>
            {data => <LoadEvents {...data} />}
          </Utility>
        </CardDeck>
      </Container>
    </StudentDashboardLayout>
  )
};

export default StudentCalendar;
