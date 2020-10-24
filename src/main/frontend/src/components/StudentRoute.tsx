import React from "react";
import Login from "./Login";
import { RouteProps} from "react-router";
import { Route } from "react-router-dom";


interface StudentRouteProps extends Omit<RouteProps, 'component'> {
  component: React.ComponentType<StudentComponentProps>
  apiKey: ApiKey | null,
  setApiKey: (data: ApiKey | null) => void
}

function StudentRoute({
  component: StudentComponent,
  apiKey,
  setApiKey,
  ...rest
}: StudentRouteProps) {

  const isAuthenticated = apiKey != null &&
    apiKey.creationTime + apiKey.duration > Date.now() && apiKey.creator.kind === "STUDENT";

  return (
    <Route {...rest} >
      {isAuthenticated
        ? <StudentComponent apiKey={apiKey!} setApiKey={setApiKey} />
        : <Login setApiKey={setApiKey} />}
    </Route>
  );
}

export default StudentRoute;
