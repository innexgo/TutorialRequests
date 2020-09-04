import React from 'react';
import { BrowserRouter, Route, Switch } from 'react-router-dom';

import AuthenticatedRoute from './components/AuthenticatedRoute';
import StudentRoute from './components/StudentRoute';

import Home from './pages/Home';
import About from './pages/About';
import TermsOfService from './pages/TermsOfService';
import UserDashboard from './pages/UserDashboard';
import UserApptCreator from './pages/UserApptCreator';
import Pending from './pages/Pending';
import Attendance from './pages/Attendance';
import Admin from './pages/Admin';
import Register from './pages/Register';
import RegisterConfirm from './pages/RegisterConfirm';

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

function App() {
  const [apiKey, setApiKeyState] = React.useState(getPreexistingApiKey());
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
        <AuthenticatedRoute path="/user"  {...apiKeyGetSetter}
          component={UserDashboard} />
        <AuthenticatedRoute path="/userapptcreator" {...apiKeyGetSetter}
          component={UserApptCreator}/>
        <AuthenticatedRoute path="/pending" {...apiKeyGetSetter}
          component={Pending} />
        <AuthenticatedRoute path="/attendance" {...apiKeyGetSetter}
          component={Attendance} /> 
        <AuthenticatedRoute path="/admin" {...apiKeyGetSetter}
          component={Admin} />
          <AuthenticatedRoute path="/register" {...apiKeyGetSetter}
          component={Register} />
          <AuthenticatedRoute path="/registerconfirm" {...apiKeyGetSetter}
          component={RegisterConfirm} />
        <StudentRoute path="/student" {...apiKeyGetSetter}
          component={StudentDashboard} />
        <StudentRoute path="/studentapptcreator" {...apiKeyGetSetter}
          component={StudentApptCreator} />
        <Route path="/" component={Error404} />
      </Switch>
    </BrowserRouter>
  );
}

export default App;
