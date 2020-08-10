import React from 'react';
import FullCalendar  from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import DashboardLayout from '../components/DashboardLayout';
import Utility from '../components/Utility';
import Loader from '../components/Loader';
import { Async } from 'react-async';
import { fetchApi } from '../utils/utils';
import moment from 'moment';

interface AppointmentCardProps {
  name: string
  date: string
  time: string
}

//card that shows appointment
function AppointmentCard(props: AppointmentCardProps) {
  
  return (
    

}


function StudentDashboard(props: StudentComponentProps) {
  const loadData = async (apiKey: ApiKey):Promise<UpcomingClassesProps>

  return (
  <DashboardLayout name={props.student.name} logoutCallback={()=>props.setStudent(null)} >
    <FullCalendar
      plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
      headerToolbar={{
        left: 'prev,next today',
        center: 'title',
        //may want to remove day view
        right: 'dayGridMonth,timeGridWeek,timeGridDay'
      }}
      initialView='dayGridMonth'
      //may want to disable
      editable={true}
      selectable={true}
      selectMirror={true}
      dayMaxEvents={true}
      weekends={false}
      initialEvents={[]}
      dateClick={this.handleDateClick}
    />
    </DashboardLayout>
);

handleDateClick = (


}

export default StudentDashboard;
