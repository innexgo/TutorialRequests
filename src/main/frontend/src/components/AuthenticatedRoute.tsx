import React from "react";
import Login from "../components/Login";
import { RouteProps } from "react-router";
import { Route } from "react-router-dom";


interface AuthenticatedRouteProps extends Omit<RouteProps, 'component'> {
  component: React.ComponentType<AuthenticatedComponentProps>
  apiKey: ApiKey | null,
  setApiKey: (data: ApiKey | null) => void
}

function AuthenticatedRoute({
  component: AuthenticatedComponent,
  apiKey,
  setApiKey,
  ...rest
}: AuthenticatedRouteProps) {

  const isAuthenticated = apiKey != null &&
    apiKey.creationTime + apiKey.duration > Date.now() &&
    apiKey.canLogIn &&
    apiKey.canReadUser &&
    apiKey.canWriteUser &&
    apiKey.canChangePassword &&
    apiKey.canReadApptRequest &&
    apiKey.canWriteApptRequest &&
    apiKey.canReadAppt &&
    apiKey.canWriteAppt &&
    apiKey.canReadAttendance &&
    apiKey.canWriteAttendance;

  return (
    <Route {...rest} >
      {isAuthenticated
        ? <AuthenticatedComponent apiKey={apiKey!} setApiKey={setApiKey} />
        : <Login setApiKey={setApiKey}
          canLogIn={true}
          canReadUser={true}
          canWriteUser={true}
          canChangePassword={true}
          canReadApptRequest={true}
          canWriteApptRequest={true}
          canReadAppt={true}
          canWriteAppt={true}
          canReadAttendance={true}
          canWriteAttendance={true}
        />}
    </Route>
  );
}

export default AuthenticatedRoute;
