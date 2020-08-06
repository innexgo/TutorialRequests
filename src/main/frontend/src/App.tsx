import React from 'react';
import { BrowserRouter, Route, Switch } from 'react-router-dom';

import AuthenticatedRoute from './components/AuthenticatedRoute';

import Home from './pages/Home';
import About from './pages/About';
import TermsOfService from './pages/TermsOfService';
import TeacherCalendar from './pages/Teachers/Calendar';
import StudentCalendar from './pages/Students/Calendar';
import StudentMakeAppt from './pages/Students/MakeAppt';
import TeacherMakeAppt from './pages/Teachers/MakeAppt';
import Error from './pages/Error';

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
        <Route path="/" exact component={Home} />
        <Route path="/about" component={About} />
        <Route path="/terms_of_service" component={TermsOfService} />
        <AuthenticatedRoute path="/teachers"  {...apiKeyGetSetter} component={TeacherCalendar} />
        <AuthenticatedRoute path="/students" {...apiKeyGetSetter} component={StudentCalendar} />
        <AuthenticatedRoute path="/studentmakeappt" {...apiKeyGetSetter} component={StudentMakeAppt} />
        <AuthenticatedRoute path="/teachermakeappt" {...apiKeyGetSetter} component={TeacherMakeAppt} />
        <Route path="/" component={Error} />
      </Switch>
    </BrowserRouter>
  );
}

export default App;
