import React from 'react'
import FullCalendar, { DateSelectArg, EventInput, EventClickArg } from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import UserDashboardLayout from '../components/UserDashboardLayout';
import UserCalendarCard from '../components/UserCalendarCard';

import { Popover, Container, CardDeck } from 'react-bootstrap';
import { fetchApi } from '../utils/utils';
import UtilityWrapper from '../components/UtilityWrapper';

import CreateApptModal from '../components/CreateApptModal';
import ReviewApptRequestModal from '../components/ReviewApptRequestModal';
import ApptTakeAttendanceModal from '../components/ApptTakeAttendanceModal';
import AttendanceInfoModal from '../components/AttendanceInfoModal';

function EventCalendar(props: AuthenticatedComponentProps) {

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

  const [showCreateApptModal, setShowCreateApptModal] = React.useState(false);
  const [showReviewApptRequestModal, setShowReviewApptRequestModal] = React.useState(false);
  const [showTakeAttendanceApptModal, setShowTakeAttendanceApptModal] = React.useState(false);
  const [showAttendanceInfoModal, setShowAttendanceInfoModal] = React.useState(false);


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

  const clickHandler = (eca: EventClickArg) => {
    const props = eca.event.extendedProps;
    switch (props.kind) {
      case "ApptRequest": {
        setApptRequest(props.apptRequest);
        setShowReviewApptRequestModal(true);
        setShowTakeAttendanceApptModal(false);
        setShowAttendanceInfoModal(false);
        break;
      }
      case "Appt": {
        setAppt(props.appt);
        setShowTakeAttendanceApptModal(true);
        setShowReviewApptRequestModal(false);
        setShowAttendanceInfoModal(false);
        break;
      }
      case "Attendance": {
        setAttendance(props.attendance);
        setShowAttendanceInfoModal(true);
        setShowReviewApptRequestModal(false);
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
        eventContent={UserCalendarCard}
        unselectCancel=".CreateApptModal"
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
      {apptRequest == null ? <> </> :
        <ReviewApptRequestModal
          show={showReviewApptRequestModal}
          setShow={setShowReviewApptRequestModal}
          apptRequest={apptRequest}
          apiKey={props.apiKey}
        />
      }
      {appt == null ? <> </> :
        <ApptTakeAttendanceModal
          show={showTakeAttendanceApptModal}
          setShow={setShowTakeAttendanceApptModal}
          appt={appt}
          apiKey={props.apiKey}
        />
      }
      {attendance == null ? <> </> :
        <AttendanceInfoModal
          show={showAttendanceInfoModal}
          setShow={setShowAttendanceInfoModal}
          attendance={attendance}
        />
      }
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
