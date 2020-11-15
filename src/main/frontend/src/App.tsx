import React from 'react';
import { BrowserRouter, Route, Switch } from 'react-router-dom';

import AuthenticatedRoute from './components/AuthenticatedRoute';
import StudentRoute from './components/StudentRoute';
import HomeRoute from './components/HomeRoute';

import TermsOfService from './pages/TermsOfService';
import Instructions from './pages/Instructions';
import UserDashboard from './pages/UserDashboard';
import Admin from './pages/Admin';
import Register from './pages/Register';
import RegisterConfirm from './pages/RegisterConfirm';
import ForgotPassword from './pages/ForgotPassword';

import StudentDashboard from './pages/StudentDashboard';

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
        <HomeRoute path="/" exact {...apiKeyGetSetter} />
        <Route path="/instructions" component={Instructions} />
        <Route path="/terms_of_service" component={TermsOfService} />
        <Route path="/register" component={Register} />
        <Route path="/forgot_password" component={ForgotPassword} />
        <Route  path="/register_confirm" component={RegisterConfirm} />
        <AuthenticatedRoute path="/user"  {...apiKeyGetSetter}
          component={UserDashboard} />
        <AuthenticatedRoute path="/admin" {...apiKeyGetSetter}
          component={Admin} />
        <StudentRoute path="/student" {...apiKeyGetSetter}
          component={StudentDashboard} />
        <Route path="/" component={Error404} />
      </Switch>
    </BrowserRouter>
  );
}

export default App;
