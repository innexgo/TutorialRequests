import React from 'react'
import FullCalendar, { EventInput }  from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import DashboardLayout from '../components/DashboardLayout';

import {Popover, Container, CardDeck} from 'react-bootstrap';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface ApptProps {
  appointments: ApptRequest[],
}

function LoadEvents(props: ApptProps){
  const events = props.appointments;

  const INITIAL_EVENTS: EventInput[] = 
    events.map((x) =>
    ({
      id: `${x.id}`,
      title: `${x.user.name}`,
      start: `${moment(x.requestTime).format("yyyy-mm-dd[T]h:mm:ss")}`,
      end: `${moment(x.requestTime + x.requestDuration).format("yyyy-mm-dd[T]h:mm:ss")}`,
      allDay: false
     })
     );

    return(
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
      initialEvents={INITIAL_EVENTS}
    />
  </>
  );
 } 

function StudentCalendar(props: AuthenticatedComponentProps) {
  const loadData = async (apiKey: ApiKey):Promise<ApptProps> => {
    const appointments = await fetchApi('apptRequest/?' + new URLSearchParams([
      ['offset', '0'],
      ['count', '0xFFFFFFFF'],
      ['user_id', `${apiKey.user.id}`],
      ['approved', 'true'],
      ['apiKey', apiKey.key]
    ])) as ApptRequest[];
    return {
      appointments
    }
  };
  
  const informationTooltip = <Popover id="information-tooltip">
    This screen shows all future appointments. You can click any date to add an appointment on that date, or click an existing appointment to delete it.
  </Popover>;

  return (
  <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
    <Container fluid className="py-3 px-3">
    <CardDeck>
      <Utility title="Calendar" overlay={informationTooltip}>
      <Async promise={loadData(props.apiKey)}>
        <Async.Pending><Loader /></Async.Pending>
        <Async.Fulfilled>
          {data => <LoadEvents {...(data as ApptProps)} />}
        </Async.Fulfilled>
        <Async.Rejected>{error => `Something went wrong: ${error.message}`}</Async.Rejected>
      </Async>
      </Utility>
    </CardDeck>
    </Container>
  </DashboardLayout>
)};

export default StudentCalendar;
