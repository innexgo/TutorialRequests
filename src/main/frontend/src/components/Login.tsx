import React from 'react';
import { Button, Form, } from 'react-bootstrap'

import { fetchApi } from '../utils/utils';

interface LoginProps {
  setApiKey: (data: ApiKey | null) => void
}

function Login(props: LoginProps) {
  const errorStyle = {
    color: "#DC143C"
  }

  const formBoxStyle = {
    borderLeft: '0',
    borderTop: '0',
    borderRight: '0',
    borderRadius: '0',
    borderBottom: '1px solid grey',
  };

  const [errorText, setErrorText] = React.useState("");
  const [userName, setUserName] = React.useState("");
  const [password, setPassword] = React.useState("");

  async function postLogin() {
    try {
      const apiKey = await fetchApi(`apiKey/new/?` + new URLSearchParams([
        ['userEmail', userName],
        ['userPassword', password],
        ['duration', `${5 * 60 * 60 * 1000}`], // 5 hours
      ])) as ApiKey;
      props.setApiKey(apiKey);
    } catch (e) {
      console.log(e);
      setErrorText("Your username or password is incorrect");
    }
  }

  return <Form>
    <Form.Group>
      <Form.Control style={formBoxStyle} id="username" type="email" placeholder="Email"
        onChange={e => {
          setUserName(e.target.value);
        }} />
      <br />
      <Form.Control style={formBoxStyle} id="password" type="password" placeholder="Password"
        onChange={e => {
          setPassword(e.target.value);
        }} />
      <p className="form-text text-danger" id="error"></p>
    </Form.Group>
    <Button variant="dark" onClick={async () => postLogin()}>Login</Button>
    <p style={errorStyle}>{errorText}</p>
  </Form>
}

export default Login;
