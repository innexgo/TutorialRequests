import * as React from "react";
import { BrowserRouter, Route, Switch } from "react-router-dom";

import AuthenticatedRoute from './components/AuthenticatedRoute';

import Students from "./pages/Students";
import Teachers from "./pages/Teachers";


function getPreexistingApiKey() {
  const preexistingApiKeyString = localStorage.getItem("apiKey");
  if (preexistingApiKeyString == null) {
    return null;
  } else {
    try {
      // TODO validate here
      return JSON.parse(preexistingApiKeyString) as ApiKey;
    } catch (e) {
      // try to clean up a bad config
      localStorage.setItem("apiKey", JSON.stringify(null));
      return null;
    }
  }
}

function App() {
  const preexistingApiKey = getPreexistingApiKey();

  const [apiKey, setApiKeyState] = React.useState(preexistingApiKey);

	// contains methods to get and set the api key, while updating the local store as well
  const apiKeyGetSetter = {
    apiKey: apiKey,
    setApiKey: (data: ApiKey | null) => {
      localStorage.setItem("apiKey", JSON.stringify(data));
      setApiKeyState(data);
    }
  };

  return (
    <BrowserRouter>
      <Switch>
        <AuthenticatedRoute {...apiKeyGetSetter} path="/students" exact component={Students} />
        <AuthenticatedRoute {...apiKeyGetSetter} path="/teachers" exact component={Teachers} />
      </Switch>
    </BrowserRouter>);
}

export default App;
