import React from 'react';
import { Home, Settings } from '@material-ui/icons';

import DashboardLayout from '../components/DashboardLayout';

export default function(props: React.PropsWithChildren<AuthenticatedComponentProps>) {
  return (<DashboardLayout name={props.apiKey.user.name} logoutCallback={() => props.setApiKey(null)} >
    <DashboardLayout.SidebarEntry label="Home" icon={Home} href="/student" />
    <DashboardLayout.SidebarEntry label="Settings" icon={Settings} href="/settings" />
    <DashboardLayout.Body>
      {props.children}
    </DashboardLayout.Body>
  </DashboardLayout>)
}
