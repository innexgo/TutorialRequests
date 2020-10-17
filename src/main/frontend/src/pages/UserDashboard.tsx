import React from 'react'
import FullCalendar, { DateSelectArg, EventInput, EventClickArg } from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import UserDashboardLayout from '../components/UserDashboardLayout';
import SearchUserDropdown from '../components/SearchUserDropdown';
import UserCalendarCard from '../components/UserCalendarCard';

import { Row, Col, Popover, Container, CardDeck, Modal, Button, Form } from 'react-bootstrap';
import UtilityWrapper from '../components/UtilityWrapper';
import { fetchApi } from '../utils/utils';
import format from 'date-fns/format';

type CreateApptModalProps = {
  show: boolean;
  start: number;
  duration: number;
  setShow: (show: boolean) => void;
  apiKey: ApiKey;
}

function CreateApptModal(props: CreateApptModalProps) {
  const [studentId, setStudentId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  const submit = async () => {
    const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['targetId', `${studentId}`],
      ["attending", 'false'],
      ['message', message],
      ['startTime', `${props.start}`],
      ['duration', `${props.duration}`],
      ['apiKey', `${props.apiKey.key}`],
    ])) as ApptRequest;

    await fetchApi('appt/new/?' + new URLSearchParams([
      ["apptRequestId", `${apptRequest.apptRequestId}`],
      ["message", message],
      ["startTime", `${props.start}`],
      ["duration", `${props.duration}`],
      ["apiKey", `${props.apiKey.key}`]
    ]));
    props.setShow(false);
  }

  return <Modal
    className="CreateApptModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Create Appointment with Student</Modal.Title>
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
        <Form.Group as={Row} controlId="student">
          <Form.Label column sm={2}>Student ID</Form.Label>
          <Col>
            <SearchUserDropdown apiKey={props.apiKey} userKind={"STUDENT"} setFn={e => setStudentId(e)} />
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
        <Button variant="primary" disabled={studentId == null} onClick={submit}>
          Submit
        </Button>
      </Form>
    </Modal.Body>
  </Modal>
}


function EventCalendar(props: AuthenticatedComponentProps) {

  const apptRequestToEvent = (x: ApptRequest): EventInput => ({
    id: `${x.apptRequestId}`,
    start: new Date(x.startTime),
    end: new Date(x.startTime + x.duration),
    color: "#00000000",
    kind: "ApptRequest",
    apiKey: props.apiKey,
    apptRequest: x
  })

  const apptToEvent = (x: Appt): EventInput => ({
    id: `${x.apptRequest.apptRequestId}`,
    start: new Date(x.startTime),
    end: new Date(x.startTime + x.duration),
    color: "#00000000",
    kind: "Appt",
    apiKey: props.apiKey,
    appt: x
  })

  const attendanceToEvent = (x: Attendance): EventInput => ({
    id: `${x.appt.apptRequest.apptRequestId}`,
    start: new Date(x.appt.startTime),
    end: new Date(x.appt.startTime + x.appt.duration),
    color: "#00000000",
    kind: "Attendance",
    apiKey: props.apiKey,
    attendance: x
  })

  const [start, setStart] = React.useState(0);
  const [duration, setDuration] = React.useState(0);

  const [showCreateApptModal, setShowCreateApptModal] = React.useState(false);

  const calendarRef = React.useRef<FullCalendar | null>(null);

  const eventSource = async (
    args: {
      start: Date;
      end: Date;
      startStr: string;
      endStr: string;
      timeZone: string;
    }) => {

    const localApptRequests = await fetchApi(`apptRequest/?` + new URLSearchParams([
      ['hostId', `${props.apiKey.creator.id}`],
      ['minStartTime', `${args.start.valueOf()}`],
      ['maxStartTime', `${args.end.valueOf()}`],
      ['confirmed', 'false'],
      ['apiKey', `${props.apiKey.key}`],
    ])) as ApptRequest[];

    const localAppts = await fetchApi('appt/?' + new URLSearchParams([
      ["hostId", `${props.apiKey.creator.id}`],
      ['minStartTime', `${args.start.valueOf()}`],
      ['maxStartTime', `${args.end.valueOf()}`],
      ['attended', 'false'],
      ["apiKey", `${props.apiKey.key}`]
    ])) as Appt[];

    const localAttendances = await fetchApi('attendance/?' + new URLSearchParams([
      ["hostId", `${props.apiKey.creator.id}`],
      ['minStartTime', `${args.start.valueOf()}`],
      ['maxStartTime', `${args.end.valueOf()}`],
      ["apiKey", `${props.apiKey.key}`]
    ])) as Attendance[];

    return [
      ...localApptRequests.map(apptRequestToEvent),
      ...localAppts.map(apptToEvent),
      ...localAttendances.map(attendanceToEvent),
    ];
  }

  return (
    <div>
      <FullCalendar
        ref={calendarRef}
        plugins={[timeGridPlugin, interactionPlugin]}
        headerToolbar={{
          left: 'prev,next today',
          center: '',
          right: 'timeGridDay,timeGridWeek',
        }}
        initialView='timeGridWeek'
        height={"80vh"}
        allDaySlot={false}
        nowIndicator={true}
        editable={false}
        selectable={true}
        selectMirror={true}
        events={eventSource}
        eventContent={UserCalendarCard}
        unselectCancel=".CreateApptModal"
        slotMinTime="08:00"
        slotMaxTime="18:00"
        weekends={false}
        expandRows={true}
        businessHours={{
          daysOfWeek: [1, 2, 3, 4, 5], // MTWHF
          startTime: '08:00', // 8am
          endTime: '18:00' // 6pm
        }}
        selectConstraint="businessHours"
        select={(dsa: DateSelectArg) => {
          setStart(dsa.start.valueOf());
          setDuration(dsa.end.valueOf() - dsa.start.valueOf());
          setShowCreateApptModal(true);
        }}
        unselect={() => {
          setShowCreateApptModal(false);
        }}
      />
      <CreateApptModal
        apiKey={props.apiKey}
        show={showCreateApptModal}
        setShow={(a: boolean) => {
          setShowCreateApptModal(a)
          if (!a && calendarRef.current != null) {
            calendarRef.current.getApi().unselect();
          }
        }}
        start={start}
        duration={duration}
      />
    </div>
  )
}


function UserDashboard(props: AuthenticatedComponentProps) {
  return (
    <UserDashboardLayout {...props} >
      <Container fluid className="py-3 px-3">
        <CardDeck>
          <UtilityWrapper title="Upcoming Appointments">
            <Popover id="information-tooltip">
              This screen shows all future appointments.
              You can click any date to add an appointment on that date,
              or click an existing appointment to delete it.
           </Popover>
            <EventCalendar {...props} />
          </UtilityWrapper>
        </CardDeck>
      </Container>
    </UserDashboardLayout>
  )
};

export default UserDashboard;
