import React from 'react';
import { Home, } from '@material-ui/icons';

import DashboardLayout from '../components/DashboardLayout';

export default function(props: React.PropsWithChildren<AuthenticatedComponentProps>) {
  return (<DashboardLayout name={props.apiKey.creator.name} logoutCallback={() => props.setApiKey(null)} >
    <DashboardLayout.SidebarEntry label="Home" icon={Home} href="/user" />
    <DashboardLayout.Body>
      {props.children}
    </DashboardLayout.Body>
  </DashboardLayout>)
}
