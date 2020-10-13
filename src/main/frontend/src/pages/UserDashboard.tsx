import React, { useState } from 'react'
import FullCalendar, { DateSelectArg, EventApi } from '@fullcalendar/react'
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
  return <Modal
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
          <Form.Control disabled>
            {format(props.start, "MMM Do, hh:mm a")}
          </Form.Control>
        </Form.Group>
        <Form.Group controlId="endTime">
          <Form.Label>End Time</Form.Label>
          <Form.Control disabled>
            {format(props.start + props.duration, "MMM Do, hh:mm a")}
          </Form.Control>
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
        <Button variant="primary" disabled={studentId == null} onClick={async () => props.createAppt(studentId!, message)}>
          Submit
        </Button>
      </Form>
    </Modal.Body>
  </Modal>
}



function EventCalendar(props: AuthenticatedComponentProps) {

  let [start, setStart] = React.useState<number | null>(null);
  let [duration, setDuration] = React.useState<number | null>(null);
  let [showCreateApptModal, setShowCreateApptModal] = React.useState(false);

  let [apptRequests, setApptRequests] = React.useState<ApptRequest[]>([]);
  let [appts, setAppts] = React.useState<Appt[]>([]);
  let [attendances, setAttendances] = React.useState<Attendance[]>([]);

  setApptRequests(my


  const getEvents = () => {
    type PartialEvent = {
      id: string,
      title: string,
      start: Date,
      end: Date,
      color: string
    }

    const apptRequestEvents: PartialEvent[] = apptRequests.map((x: ApptRequest) => ({
      id: `${x.apptRequestId}`,
      title: x.attendee.name,
      start: new Date(x.startTime),
      end: new Date(x.startTime + x.duration),
      color: "red"
    }));

    const apptEvents: PartialEvent[] = appts.map((x: Appt) => ({
      id: `${x.apptRequest.apptRequestId}`,
      title: x.apptRequest.attendee.name,
      start: new Date(x.startTime),
      end: new Date(x.startTime + x.duration),
      color: "green"
    }));

    const attendanceEvents: PartialEvent[] = attendances.map((x: Attendance) => ({
      id: `${x.appt.apptRequest.apptRequestId}`,
      title: x.appt.apptRequest.attendee.name,
      start: new Date(x.appt.startTime),
      end: new Date(x.appt.startTime + x.appt.duration),
      color: "blue"
    }));

    return [...apptEvents, ...apptRequestEvents, ...attendanceEvents];
  }


  const createAppt = async (studentId: number, message: string) => {
    const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['attending', 'false'],
      ['targetId', `${studentId}`],
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

    setAppts([...appts, appt])
  }

  const handleDateSelect = () => {

  }

  return (
    <>
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
        events={getEvents()}
      />
      {(start != null) && (duration != null)
        ? <CreateApptModal
          apiKey={props.apiKey}
          createAppt={createAppt}
          show={showCreateApptModal}
          setShow={setShowCreateApptModal}
          start={start}
          duration={duration} />
        : <></>
      }
    </>
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
