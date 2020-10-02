import React from "react";
import { RouteProps, Redirect } from "react-router";
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
    apiKey.creationTime + apiKey.duration > Date.now() && apiKey.user.kind == "STUDENT";

  return (
    <Route {...rest} >
      {isAuthenticated
        ? <AuthenticatedComponent apiKey={apiKey!} setApiKey={setApiKey} />
        : <Redirect to="/" />}
    </Route>
  );
}

export default StudentRoute;
