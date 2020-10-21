import React from 'react'
import FullCalendar, { DateSelectArg, EventInput, EventClickArg } from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import StudentDashboardLayout from '../components/StudentDashboardLayout';
import StudentCalendarCard from '../components/StudentCalendarCard';

import { Popover, Container, CardDeck } from 'react-bootstrap';
import { fetchApi } from '../utils/utils';
import UtilityWrapper from '../components/UtilityWrapper';

import CreateApptRequestModal from '../components/CreateApptRequestModal';
import ViewApptRequestModal from '../components/ViewApptRequestModal';
import ViewApptModal from '../components/ViewApptModal';
import ViewAttendanceModal from '../components/ViewAttendanceModal';

function StudentEventCalendar(props: StudentComponentProps) {

  const apptRequestToEvent = (x: ApptRequest): EventInput => ({
    id: `${x.apptRequestId}`,
    start: new Date(x.startTime),
    end: new Date(x.startTime + x.duration),
    color: "#00000000",
    kind: "ApptRequest",
    apptRequest: x
  })

  const apptToEvent = (x: Appt): EventInput => ({
    id: `${x.apptRequest.apptRequestId}`,
    start: new Date(x.startTime),
    end: new Date(x.startTime + x.duration),
    color: "#00000000",
    kind: "Appt",
    appt: x
  })

  const attendanceToEvent = (x: Attendance): EventInput => ({
    id: `${x.appt.apptRequest.apptRequestId}`,
    start: new Date(x.appt.startTime),
    end: new Date(x.appt.startTime + x.appt.duration),
    color: "#00000000",
    kind: "Attendance",
    attendance: x
  })

  const [start, setStart] = React.useState(0);
  const [duration, setDuration] = React.useState(0);

  const [showCreateApptRequestModal, setShowCreateApptRequestModal] = React.useState(false);
  const [showViewApptRequestModal, setShowViewApptRequestModal] = React.useState(false);
  const [showTakeAttendanceApptModal, setShowTakeAttendanceApptModal] = React.useState(false);
  const [showViewAttendanceModal, setShowViewAttendanceModal] = React.useState(false);


  const [appt, setAppt] = React.useState<Appt | null>(null);
  const [attendance, setAttendance] = React.useState<Attendance | null>(null);
  const [apptRequest, setApptRequest] = React.useState<ApptRequest | null>(null);

  const calendarRef = React.useRef<FullCalendar | null>(null);

  const eventSource = async (
    args: {
      start: Date;
      end: Date;
      startStr: string;
      endStr: string;
      timeZone: string;
    }) => {

    console.log("nice");

    const localApptRequests = await fetchApi(`apptRequest/?` + new URLSearchParams([
      ['attendeeId', `${props.apiKey.creator.id}`],
      ['minStartTime', `${args.start.valueOf()}`],
      ['maxStartTime', `${args.end.valueOf()}`],
      ['confirmed', 'false'],
      ['apiKey', `${props.apiKey.key}`],
    ])) as ApptRequest[];

    const localAppts = await fetchApi('appt/?' + new URLSearchParams([
      ["attendeeId", `${props.apiKey.creator.id}`],
      ['minStartTime', `${args.start.valueOf()}`],
      ['maxStartTime', `${args.end.valueOf()}`],
      ['attended', 'false'],
      ["apiKey", `${props.apiKey.key}`]
    ])) as Appt[];

    const localAttendances = await fetchApi('attendance/?' + new URLSearchParams([
      ["attendeeId", `${props.apiKey.creator.id}`],
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

  const clickHandler = (eca: EventClickArg) => {
    const props = eca.event.extendedProps;
    switch (props.kind) {
      case "ApptRequest": {
        setApptRequest(props.apptRequest);
        setShowViewApptRequestModal(true);
        setShowTakeAttendanceApptModal(false);
        setShowViewAttendanceModal(false);
        break;
      }
      case "Appt": {
        setAppt(props.appt);
        setShowTakeAttendanceApptModal(true);
        setShowViewApptRequestModal(false);
        setShowViewAttendanceModal(false);
        break;
      }
      case "Attendance": {
        setAttendance(props.attendance);
        setShowViewAttendanceModal(true);
        setShowViewApptRequestModal(false);
        setShowTakeAttendanceApptModal(false);
        break;
      }
    }
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
        eventContent={StudentCalendarCard}
        unselectCancel=".CreateApptRequestModal"
        slotMinTime="08:00"
        slotMaxTime="18:00"
        weekends={false}
        eventClick={clickHandler}
        expandRows={true}
        businessHours={{
          daysOfWeek: [1, 2, 3, 4, 5], // MTWHF
          startTime: "08:00", // 8am
          endTime: "18:00", // 6pm
          startRecur: new Date()
        }}
        selectConstraint="businessHours"
        select={(dsa: DateSelectArg) => {
          // only open modal if this date is in the future
          if (dsa.start.valueOf() > Date.now()) {
            setStart(dsa.start.valueOf());
            setDuration(dsa.end.valueOf() - dsa.start.valueOf());
            setShowCreateApptRequestModal(true);
          } else {
            if (calendarRef.current != null) {
              calendarRef.current.getApi().unselect();
            }
          }
        }}
        unselect={() => {
          setShowCreateApptRequestModal(false);
        }}
      />
      <CreateApptRequestModal
        apiKey={props.apiKey}
        show={showCreateApptRequestModal}
        setShow={(a: boolean) => {
          setShowCreateApptRequestModal(a)
          if (!a && calendarRef.current != null) {
            calendarRef.current.getApi().unselect();
          }
        }}
        start={start}
        duration={duration}
      />
      {apptRequest == null ? <> </> :
        <ViewApptRequestModal
          show={showViewApptRequestModal}
          setShow={setShowViewApptRequestModal}
          apptRequest={apptRequest}
          apiKey={props.apiKey}
        />
      }
      {appt == null ? <> </> :
        <ViewApptModal
          show={showTakeAttendanceApptModal}
          setShow={setShowTakeAttendanceApptModal}
          appt={appt}
          apiKey={props.apiKey}
        />
      }
      {attendance == null ? <> </> :
        <ViewAttendanceModal
          show={showViewAttendanceModal}
          setShow={setShowViewAttendanceModal}
          attendance={attendance}
        />
      }
    </div>
  )
}

function StudentDashboard(props: StudentComponentProps) {
  return (
    <StudentDashboardLayout {...props} >
      <Container fluid className="py-3 px-3">
        <CardDeck>
          <UtilityWrapper title="Upcoming Appointments">
            <Popover id="information-tooltip">
              This screen shows your upcoming appointments appointments.
              You can click any appointment to learn more about it,
              or drag your mouse to select a new one.
           </Popover>
            <StudentEventCalendar {...props} />
          </UtilityWrapper>
        </CardDeck>
      </Container>
    </StudentDashboardLayout>
  )
};

export default StudentDashboard;
