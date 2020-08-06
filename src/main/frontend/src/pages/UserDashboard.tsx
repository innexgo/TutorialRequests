import React from 'react'
import FullCalendar  from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import DashboardLayout from '../components/DashboardLayout';

function TeacherCalendar(props: AuthenticatedComponentProps) {
  return (
  <DashboardLayout name={props.apiKey.user.name} logoutCallback={()=>props.setApiKey(null)} >
    <FullCalendar
      plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
      headerToolbar={{
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay'
      }}
      initialView='dayGridMonth'
      editable={true}
      selectable={true}
      selectMirror={true}
      dayMaxEvents={true}
      weekends={false}
      initialEvents={[]}
    />
  </DashboardLayout>
)};

export default TeacherCalendar;
