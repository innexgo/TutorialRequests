import React from "react";
import HomePage from "../components/Home"
import { RouteProps } from "react-router";
import { Route, Redirect } from "react-router-dom";


interface HomeRouteProps extends Omit<RouteProps, 'component'> {
  component: React.ComponentType<AuthenticatedComponentProps>
  apiKey: ApiKey | null,
  setApiKey: (data: ApiKey | null) => void
}

function Home({
  component: AuthenticatedComponent,
  apiKey,
  setApiKey,
  ...rest
}: HomeRouteProps) {

  const isAuthenticated = apiKey != null && apiKey.creationTime + apiKey.duration > Date.now();
  const renderResult = () => { 
    if(apiKey != null && apiKey.user.kind == "STUDENT"){
      return <Redirect to="/student" /> 
    }
    else{
      return <Redirect to="/user" />  
    }
  }

  return (
    <Route {...rest} >
      {isAuthenticated
        ? renderResult()
        : <HomePage setApiKey={setApiKey} />}
    </Route>
  );
}

export default Home;
