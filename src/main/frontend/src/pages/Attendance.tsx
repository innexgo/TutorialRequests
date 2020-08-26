import React from 'react';
import { Card, Button, Popover, Container, CardDeck } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import AttendCard from '../components/AttendCard';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface AttendanceProps {
  appointments: Appt[],
  apiKey: ApiKey,
}

function Attendees(props: AttendanceProps) {

  const now = Date.now();
  const todayAppts = props.appointments
  //sort alphabetically by student name
  .sort((a, b) => a.attendee.name.localeCompare(b.attendee.name));

  return (
  <>
    {
      todayAppts.map((x) =>
        <AttendCard
          student={x.attendee.name}
          apptId={x.id}
          time={moment(x.startTime).format("h mm a")}
          apiKey={props.apiKey}
          />
      )
    }
    </>
  );
}


export default function Attendance(props: AuthenticatedComponentProps) {
  const headerStyle = {
    margin: '2%',
    textAlign: 'center' as const,
  }

    const loadData = async (apiKey: ApiKey):Promise<AttendanceProps> => {
    const appointments = await fetchApi('appt/?' + new URLSearchParams([
      ['offset', '0'],
      ['count', '0xFFFFFFFF'],
      ['hostId', `${apiKey.user.id}`],
      ['minTime', `${moment().startOf('day')}`],
      ['maxTime', `${moment().endOf('day')}`],
      ['apiKey', apiKey.key]
    ])) as Appt[];
    return {
      appointments,
      apiKey
    }
  };

  const informationTooltip = <Popover id="information-tooltip">
    Every day during tutorial, take attendance of your students here.
  </Popover>;

  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
      <Container fluid className="py-3 px-3">
      <CardDeck>
        <Utility title="Attendance" overlay={informationTooltip}>
        <Async promise={loadData(props.apiKey)}>
          <Async.Pending><Loader /></Async.Pending>
          <Async.Fulfilled>
            {data => <Attendees {...(data as AttendanceProps)} />}
          </Async.Fulfilled>
          <Async.Rejected>{error => `Something went wrong: ${error.message}`}</Async.Rejected>
        </Async>
        </Utility>
      </CardDeck>
      </Container>
    </DashboardLayout>
  );
}
