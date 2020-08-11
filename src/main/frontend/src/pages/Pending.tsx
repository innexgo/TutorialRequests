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

export default function Pending(props: AuthenticatedComponentProps) {
  const headerStyle = {
    margin: '2%',
    textAlign: 'center' as const,
  }

  const loadData = async (apiKey: ApiKey):Promise<ApptProps> => {
    const appointments = await fetchApi("ApptRequest/new/?" + new URLSearchParams([
      ["user_id", apiKey.user.userId],
      ["reviewed", false],
      ["minRequestTime", `${Date.now()}`],
      ["apiKey", apiKey.key]
    ])) as apptRequest[];
    return {
      appointments,
    }
  };

  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
      <h1 style={headerStyle}>Pending Appointments</h1>
      <ApptCard student="Marek Pinto" date="Aug 20"/>
      <ApptCard student="Richard Le" date="Aug 23" />
    </DashboardLayout>
  );
}
