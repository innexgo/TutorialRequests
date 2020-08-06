import React from 'react';
import { Card, Button } from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';
import AttendCard from '../components/AttendCard';

export default function Pending(props: AuthenticatedComponentProps) {
  const headerStyle = {
    margin: '2%',
    textAlign: 'center' as const,
  }
  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
      <h1 style={headerStyle}>Attendance</h1>
      <AttendCard student="Marek Pinto"/>
      <AttendCard student="Richard Le"/>
    </DashboardLayout>
  );
}
