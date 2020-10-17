import React from "react";
import LoginInterface from "../components/LoginInterface"
import { RouteProps } from "react-router";
import { Route, Redirect } from "react-router-dom";


interface HomeRouteProps extends Omit<RouteProps, 'component'> {
  apiKey: ApiKey | null,
  setApiKey: (data: ApiKey | null) => void
}

function Home({
  apiKey,
  setApiKey,
  ...rest
}: HomeRouteProps) {


  const renderResult = () => { 
    const authenticated = apiKey != null && apiKey.creationTime + apiKey.duration > Date.now();
    if(!authenticated) {
      return <LoginInterface setApiKey={setApiKey} />
    } else if(apiKey!.creator.kind == "STUDENT"){
      return <Redirect to="/student" /> 
    } else {
      return <Redirect to="/user" />  
    }
  }

  return (
    <Route {...rest} >
      {renderResult()}
    </Route>
  );
}

export default Home;
