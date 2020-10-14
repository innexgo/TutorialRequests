import React from 'react'
import FullCalendar, { DateSelectArg, DateUnselectArg, EventApi, EventSourceInput, EventInput } from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import UserDashboardLayout from '../components/UserDashboardLayout';
import SearchUserDropdown from '../components/SearchUserDropdown';

import { Popover, Container, CardDeck, Modal, Button, Form } from 'react-bootstrap';
import UtilityWrapper from '../components/UtilityWrapper';
import { fetchApi } from '../utils/utils';
import { format } from 'date-fns';

type CreateApptModalProps = {
  show: boolean;
  start: number;
  duration: number;
  setShow: (show: boolean) => void;
  apiKey: ApiKey;
  createAppt: (studentId: number, message: string) => void;
}

function CreateApptModal(props: CreateApptModalProps) {
  const [studentId, setStudentId] = React.useState<number | null>(null);
  const [message, setMessage] = React.useState("");

  const submit = async () => {
    props.createAppt(studentId!, message)
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
        <Form.Group controlId="startTime">
          <Form.Label>Start Time</Form.Label>
          <Form.Control disabled placeholder={format(props.start, "MMM do, hh:mm a")} />
        </Form.Group>
        <Form.Group controlId="endTime">
          <Form.Label>End Time</Form.Label>
          <Form.Control disabled placeholder={format(props.start + props.duration, "MMM do, hh:mm a")} />
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
        <Button variant="primary" disabled={studentId == null} onClick={submit}>
          Submit
        </Button>
      </Form>
    </Modal.Body>
  </Modal>
}



function EventCalendar(props: AuthenticatedComponentProps) {

  type PartialEvent = {
    id: string,
    title: string,
    start: Date,
    end: Date,
    color: string
  }

  const apptRequestToEvent = (x: ApptRequest) => ({
    id: `${x.apptRequestId}`,
    title: x.attendee.name,
    start: new Date(x.startTime),
    end: new Date(x.startTime + x.duration),
    color: "red"
  })

  const apptToEvent = (x: Appt) => ({
    id: `${x.apptRequest.apptRequestId}`,
    title: x.apptRequest.attendee.name,
    start: new Date(x.startTime),
    end: new Date(x.startTime + x.duration),
    color: "green"
  })

  const attendanceToEvent = (x: Attendance) => ({
    id: `${x.appt.apptRequest.apptRequestId}`,
    title: x.appt.apptRequest.attendee.name,
    start: new Date(x.appt.startTime),
    end: new Date(x.appt.startTime + x.appt.duration),
    color: "purple"
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
        allDaySlot={false}
        nowIndicator={true}
        editable={false}
        selectable={true}
        selectMirror={true}
        events={eventSource}
        unselectCancel=".CreateApptModal"
        businessHours={{
          daysOfWeek: [1, 2, 3, 4, 5], // MTWHF
          startTime: '08:00', // 8am
          endTime: '18:00' // 6pm
        }}
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
        createAppt={async (studentId: number, message: string) => {
          const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
            ['targetId', `${studentId}`],
            ["attending", 'false'],
            ['message', message],
            ['startTime', `${start}`],
            ['duration', `${duration}`],
            ['apiKey', `${props.apiKey.key}`],
          ])) as ApptRequest;

          await fetchApi('appt/new/?' + new URLSearchParams([
            ["apptRequestId", `${apptRequest.apptRequestId}`],
            ["message", message],
            ["startTime", `${start}`],
            ["duration", `${duration}`],
            ["apiKey", `${props.apiKey.key}`]
          ]));
        }}
      />
        : <></>
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
