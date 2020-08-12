import React from 'react';
import { Card, Button } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import AttendCard from '../components/AttendCard';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface AttendanceProps {
  appointments: ApptRequest[]
}

function attendees(props: AttendanceProps) {

  const now = Date.now();
  const todayAppts = props.appointments
  //sort alphabetically
  .sort();

  return (
    {
      upcomingAppts.map((x) =>
        <AttendCard
          student={x.student.name}
          time={moment(x.requestTime).format("h mm a")}
          />
      )
    }
  );
}


export default function Attendance(props: AuthenticatedComponentProps) {
  const headerStyle = {
    margin: '2%',
    textAlign: 'center' as const,
  }

    const loadData = async (apiKey: ApiKey):Promise<ApptProps> => {
    const appointments = await fetchApi('apptRequest/new/?' + new URLSearchParams([
      ['user_id', `${apiKey.user.id}`],
      ['reviewed', "false"],
      ['minRequestTime', `${Date.now()}`],
      ['apiKey', apiKey.key]
    ])) as ApptRequest[];
    return {
      appointments,
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
            {data => <atendees {...(data as AttendanceProps)} />}
          </Async.Fulfilled>
          <Async.Rejected>{error => `Something went wrong: ${error.message}`}</Async.Rejected>
        </Async>
        </Utility>
      </CardDeck>
      </Container>
    </DashboardLayout>
  );
}
