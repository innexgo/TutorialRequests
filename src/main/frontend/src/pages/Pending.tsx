import React from 'react';
import { Card, Button } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import ApptCard from '../components/ApptCard';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface ApptProps {
  appointments: ApptRequest[]
}

function pendingAppointments(props: ApptProps) {

  const now = Date.now();
  const upcomingAppts = props.appointments
  //sort by time
  .sort((a, b) => a.startTime - b.startTime);

  return (
    {
      upcomingAppts.map((x) =>
        <ApptCard
          student={x.student.name}
          date={moment(x.requestTime).format("MMM Do")}
          message={x.message}
          />
      )
    }
  );
}


export default function Pending(props: AuthenticatedComponentProps) {
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
    This screen shows all the future appointments that students have requested. To accept the appointment, fill out an optional start/end time, an optional response, and click Accept. Click Reject to reject the appointment.
  </Popover>;

  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
      <Container fluid className="py-3 px-3">
      <CardDeck>
        <Utility title="Pending Appointments" overlay={informationTooltip}>
        <Async promise={loadData(props.apiKey)}>
          <Async.Pending><Loader /></Async.Pending>
          <Async.Fulfilled>
            {data => <pendingAppointments {...(data as ApptProps)} />}
          </Async.Fulfilled>
          <Async.Rejected>{error => `Something went wrong: ${error.message}`}</Async.Rejected>
        </Async>
        </Utility>
      </CardDeck>
      </Container>
    </DashboardLayout>
  );
}
