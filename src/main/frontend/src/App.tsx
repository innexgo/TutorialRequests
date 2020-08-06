import React from 'react';
import { BrowserRouter, Route, Switch } from 'react-router-dom';

import AuthenticatedRoute from './components/AuthenticatedRoute';
import AuthenticatedRoute from './components/StudentRoute';

import Home from './pages/Home';
import About from './pages/About';
import TermsOfService from './pages/TermsOfService';
import UserDashboard from './pages/UserDashboard';
import UserApptCreator from './pages/UserApptCreator';

import StudentDashboard from './pages/StudentDashboard';
import StudentApptCreator from './pages/StudentApptCreator';

import Error404 from './pages/Error404';

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

function getPreexistingStudent() {
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

  const [apiKey, setApiKeyState] = React.useState(getPreexistingApiKey());
  const apiKeyGetSetter = {
    apiKey: apiKey,
    setApiKey: (data: ApiKey | null) => {
      localStorage.setItem("apiKey", JSON.stringify(data));
      setApiKeyState(data);
    }
  };

  const [student, setStudent] = React.useState(getPreexistingStudent());
  const studentGetSetter = {
		
  }

  return (
    <BrowserRouter>
      <Switch>
        <Route path="/" exact component={Home} />
        <Route path="/about" component={About} />
        <Route path="/terms_of_service" component={TermsOfService} />
        <AuthenticatedRoute path="/user"  {...apiKeyGetSetter}
          component={UserDashboard} />
        <AuthenticatedRoute path="/user/apptcreator" {...apiKeyGetSetter}
          component={UserApptCreator} />
        <StudentRoute path="/student" {...studentGetSetter}
          component={StudentDashboard} />
        <StudentRoute path="/student/apptcreator" {...studentGetSetter}
          component={StudentApptCreator} />
        <Route path="/" component={Error404} />
      </Switch>
    </BrowserRouter>
  );
}

export default App;
