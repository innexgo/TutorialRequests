import React from "react";
import StudentLogin from "../components/StudentLogin";
import { RouteProps } from "react-router";
import { Route } from "react-router-dom";


interface StudentRouteProps extends Omit<RouteProps, 'component'> {
  component: React.ComponentType<StudentComponentProps>
	student: Student | null,
	setStudent: (student:Student|null)=>void
}

function StudentRoute({
    component:StudentComponent,
    student,
    setStudent,
    ...rest
  }: StudentRouteProps ) {

  return (
    <Route {...rest} >
      {student != null
          ? <StudentComponent student={student!} setStudent={setStudent} />
          : <StudentLogin setStudent={setStudent}/> }
    </Route>
  );
}

export default StudentRoute;
