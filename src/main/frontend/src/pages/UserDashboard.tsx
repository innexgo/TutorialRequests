import React, { useState } from 'react'
import FullCalendar, { DateSelectArg, EventApi } from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import UserDashboardLayout from '../components/UserDashboardLayout';
import SearchUserDropdown from '../components/SearchUserDropdown';

import { Popover, Container, CardDeck, Modal, Button, Form } from 'react-bootstrap';
import UtilityWrapper from '../components/UtilityWrapper';
import { fetchApi } from '../utils/utils';

interface EventCalendarState {
  appts: Appt[],
  apptRequests: ApptRequest[],
}

class EventCalendar extends React.Component<AuthenticatedComponentProps, EventCalendarState> {
  constructor(props: AuthenticatedComponentProps) {
    super(props)
    this.state = {
      appts: [],
      apptRequests: []
    }
  }

  createAppt(studentId: number, start: number, end: number, message: string) {
    const duration = end - start;
    const apptRequest = await fetchApi(`apptRequest/new/?` + new URLSearchParams([
      ['attending', 'false'],
      ['targetId', `${studentId}`],
      ['message', message],
      ['suggestedTime', `${start}`],
      ['apiKey', `${this.props.apiKey.key}`],
    ])) as ApptRequest;

    this.setState({
      appts: this.state.appts,
      apptRequests: [...this.state.apptRequests, apptRequest]
    })

    const appt = await fetchApi('appt/new/?' + new URLSearchParams([
      ["apptRequestId", `${apptRequest.apptRequestId}`],
      ["hostId", `${this.props.apiKey.creator.id}`],
      ["attendeeId", `${studentId}`],
      ["message", message],
      ["startTime", `${start}`],
      ["duration", `${duration}`],
      ["apiKey", `${this.props.apiKey.key}`]
    ])) as Appt;

    this.setState({
      appts: [...this.state.appts, appt],
      apptRequests: this.state.apptRequests
    })
  }

  getEvents() {
    type PartialEvent = {
      id: string,
      title: string,
      start: Date,
      end: Date,
      color: string
    }
    const apptEvents = this.state.appts.map((x: Appt) => ({
      id: `${x.apptRequest.apptRequestId}`,
      title: x.apptRequest.attendee.name,
      start: new Date(x.startTime),
      duration: new Date(x.startTime + x.duration),
      color: "blue"
    }));

    const apptRequestEvents = this.state.appts.map((x: Appt) => ({
      id: `${x.apptRequest.apptRequestId}`,
      title: x.apptRequest.attendee.name,
      start: new Date(x.startTime),
      duration: new Date(x.startTime + x.duration),
      color: "yellow"
    }));
    return [...apptEvents, ...apptRequestEvents];
  }


  render() {
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
        events={this.getEvents()}
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
