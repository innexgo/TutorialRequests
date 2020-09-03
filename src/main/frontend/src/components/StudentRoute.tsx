import React from "react";
import Login from "../components/Login";
import { RouteProps } from "react-router";
import { Route } from "react-router-dom";


interface StudentRouteProps extends Omit<RouteProps, 'component'> {
  component: React.ComponentType<AuthenticatedComponentProps>
  apiKey: ApiKey | null,
  setApiKey: (data: ApiKey | null) => void
}

function StudentRoute({
  component: AuthenticatedComponent,
  apiKey,
  setApiKey,
  ...rest
}: StudentRouteProps) {

  const isAuthenticated = apiKey != null &&
    apiKey.creationTime + apiKey.duration > Date.now() &&
    apiKey.canLogIn &&
    apiKey.canReadUser &&
    apiKey.canReadApptRequest &&
    apiKey.canWriteApptRequest &&
    apiKey.canReadAppt &&
    apiKey.canReadAttendance;

  return (
    <Route {...rest} >
      {isAuthenticated
        ? <AuthenticatedComponent apiKey={apiKey!} setApiKey={setApiKey} />
        : <Login setApiKey={setApiKey}
          canLogIn={true}
          canReadUser={true}
          canWriteUser={false}
          canChangePassword={true}
          canReadApptRequest={true}
          canWriteApptRequest={true}
          canReadAppt={true}
          canWriteAppt={false}
          canReadAttendance={true}
          canWriteAttendance={false}
        />}
    </Route>
  );
}

export default StudentRoute;
