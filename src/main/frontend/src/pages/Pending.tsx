import React from 'react';
import { Card, Button, Popover, Container, CardDeck } from 'react-bootstrap';
import UserDashboardLayout from '../components/UserDashboardLayout';
import ApptCard from '../components/ApptCard';
import Utility from '../components/Utility';
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
    .sort((a, b) => a.creationTime - b.creationTime);

  return (
    <>
      {
        upcomingAppts.map((x) =>
          <ApptCard
            student={x.creator.name}
            date={moment(x.suggestedTime).format("MMM Do")}
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

  const loadData = async (apiKey: ApiKey): Promise<ApptProps> => {
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

  return (
    <UserDashboardLayout {...props}>
      <Container fluid className="py-3 px-3">
        <CardDeck>
          <Utility<ApptProps> title="Pending Appointments" promise={loadData(props.apiKey)} >
            <Popover id="information-tooltip">
              This screen shows all the future appointments that students have requested.
              To accept the appointment, fill out an optional start/end time, an optional response,
              and click Accept. Click Reject to reject the appointment.
            </Popover>
            {data => <PendingAppointments {...data} />}
          </Utility>
        </CardDeck>
      </Container>
    </UserDashboardLayout>
  );
}
