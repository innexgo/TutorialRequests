import React from 'react';
import { Card, Button, Popover, Container, CardDeck } from 'react-bootstrap';
import UserDashboardLayout from '../components/UserDashboardLayout';
import AttendCard from '../components/AttendCard';
import Utility from '../components/Utility';
import { fetchApi } from '../utils/utils';
import format from 'date-fns/format';

interface AttendanceProps {
  appointments: Appt[],
  apiKey: ApiKey,
}

function Attendees(props: AttendanceProps) {

  const now = Date.now();
  const todayAppts = props.appointments
    //sort alphabetically by student name
    .sort((a, b) => a.apptRequest.attendee.name.localeCompare(b.apptRequest.attendee.name));

  return (
    <>
      {
        todayAppts.map((x) =>
          <AttendCard
            student={x.apptRequest.attendee.name}
            apptId={x.apptRequest.apptRequestId}
            time={format(new Date(x.startTime), "h mm a")}
            apiKey={props.apiKey}
          />
        )
      }
    </>
  );
}


export default function Attendance(props: AuthenticatedComponentProps) {
  const loadData = async (apiKey: ApiKey): Promise<AttendanceProps> => {
    const appointments = await fetchApi('appt/?' + new URLSearchParams([
      ['offset', '0'],
      ['count', '0xFFFFFFFF'],
      ['hostId', `${apiKey.creator.id}`],
      ['attended', 'false'],
      ['apiKey', apiKey.key]
    ])) as Appt[];
    return {
      appointments,
      apiKey
    }
  };

  return (
    <UserDashboardLayout {...props} >
      <Container fluid className="py-3 px-3">
        <CardDeck>
          <Utility<AttendanceProps> title="Attendance" promise={loadData(props.apiKey)}>
            <Popover id="information-tooltip">
              Every day during tutorial, take attendance of your students here.
            </Popover>
            {data => <Attendees {...data} />}
          </Utility>
        </CardDeck>
      </Container>
    </UserDashboardLayout>
  );
}
