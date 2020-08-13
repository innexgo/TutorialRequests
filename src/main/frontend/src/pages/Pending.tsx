import React from 'react';
import { Card, Button, Popover, Container, CardDeck } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import ApptCard from '../components/ApptCard';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface ApptProps {
  appointments: ApptRequest[],
  apiKey: ApiKey,
}

function PendingAppointments(props: ApptProps) {

  const now = Date.now();
  const upcomingAppts = props.appointments
  //sort by time
  .sort((a, b) => a.requestTime - b.requestTime);

  return (
  <>
    {
      upcomingAppts.map((x) =>
        <ApptCard
          student={x.student.name}
          date={moment(x.requestTime).format("MMM Do")}
          studentMessage={x.message}
          apptId={x.id}
          apiKey={props.apiKey}
          />
      )
    }
    </>
  );
}


export default function Pending(props: AuthenticatedComponentProps) {
  const headerStyle = {
    margin: '2%',
    textAlign: 'center' as const,
  }

  const loadData = async (apiKey: ApiKey):Promise<ApptProps> => {
    const appointments = await fetchApi('apptRequest/?' + new URLSearchParams([
      ['offset', '0'],
      //TODO make variable count and allow user to move back and forth with arrows
      ['count', '10'],
      ['user_id', `${apiKey.user.id}`],
      ['reviewed', "false"],
      ['minRequestTime', `${Date.now()}`],
      ['apiKey', apiKey.key]
    ])) as ApptRequest[];
    return {
      appointments,
      apiKey,
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
            {data => <PendingAppointments {...(data as ApptProps)} />}
          </Async.Fulfilled>
          <Async.Rejected>{error => `Something went wrong: ${error.message}`}</Async.Rejected>
        </Async>
        </Utility>
      </CardDeck>
      </Container>
    </DashboardLayout>
  );
}
