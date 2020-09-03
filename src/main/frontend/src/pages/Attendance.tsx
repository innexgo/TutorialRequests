import React from 'react';
import { Card, Button, Popover, Container, CardDeck } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import AttendCard from '../components/AttendCard';
import Utility from '../components/Utility';
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
  const loadData = async (apiKey: ApiKey): Promise<AttendanceProps> => {
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

  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={() => props.setApiKey(null)} >
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
    </DashboardLayout>
  );
}
