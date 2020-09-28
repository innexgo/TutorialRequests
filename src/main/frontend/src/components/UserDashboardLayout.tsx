import React from 'react';
import { Home, AccountCircle, Assessment } from '@material-ui/icons';

import DashboardLayout from '../components/DashboardLayout';

export default function(props: React.PropsWithChildren<AuthenticatedComponentProps>) {
  return (<DashboardLayout name={props.apiKey.user.name} logoutCallback={() => props.setApiKey(null)} >
    <DashboardLayout.SidebarEntry label="Home" icon={Home} href="/user" />
    <DashboardLayout.SidebarEntry label="Attendance" icon={AccountCircle} href="/attendance" />
    // <DashboardLayout.SidebarEntry label="Report (TBC)" icon={Assessment} href="/report" />
    <DashboardLayout.Body>
      {props.children}
    </DashboardLayout.Body>
  </DashboardLayout>)
}
