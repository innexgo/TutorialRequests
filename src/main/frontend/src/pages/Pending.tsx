import React from 'react';
import { Card, Button } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import ApptCard from '../components/ApptCard';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';

interface ApptProps {
  //list of upcoming appts
}

export default function Pending(props: AuthenticatedComponentProps) {
  const headerStyle = {
    margin: '2%',
    textAlign: 'center' as const,
  }
  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
      <h1 style={headerStyle}>Pending Appointments</h1>
      <ApptCard student="Marek Pinto" date="Aug 20"/>
      <ApptCard student="Richard Le" date="Aug 23" />
    </DashboardLayout>
  );
}
