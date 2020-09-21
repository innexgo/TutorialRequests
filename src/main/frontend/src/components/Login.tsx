import React from 'react';
import { Button, Card, Form } from 'react-bootstrap'

import ExternalLayout from '../components/ExternalLayout';
import { fetchApi } from '../utils/utils';

import innexgo_logo from '../img/innexgo_logo_dark.png';

import blurred_bg from '../img/homepage-bg.png';

interface LoginProps {
  setApiKey: (data: ApiKey | null) => void
}

function Login(props: LoginProps) {
  const bgStyle = {
    backgroundImage: `radial-gradient(rgba(0, 0, 0, 0.9),rgba(0, 0, 0, 0.1)), url(${blurred_bg})`,
    textColor: 'black',
    height: "100vh",
    alignItems: "center",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    backgroundSize: "cover",
    display: "flex",
    justifyContent: "center",
  };

  const errorStyle = {
    color: "#DC143C"
  }

  const formStyle={
    width: '50%',
    border: '1px solid white',
    boxShadow: '5px',
    backgroundColor: 'white',
    padding: '25px'
  }

  const formBoxStyle={
    borderLeft:'0',
    borderTop:'0',
    borderRight:'0',
    borderRadius: '0',
    borderBottom: '1px solid grey',
  }

  const [errorText, setErrorText] = React.useState("");
  const [userName, setUserName] = React.useState("");
  const [password, setPassword] = React.useState("");

  async function postLogin() {
    try {
      const apiKey = await fetchApi(`apiKey/new/?` + new URLSearchParams([
        ['userEmail', userName],
        ['userPassword', password],
        ['duration', `${5*60*60*1000}`], // 5 hours
      ])) as ApiKey;
      props.setApiKey(apiKey);
    } catch (e) {
      console.log(e);
      setErrorText("Your username or password is incorrect");
    }
  }

  return (
    <ExternalLayout fixed={true} transparentTop={true}>
      <div style={bgStyle}>
        <div style={formStyle}>
              <h4><img
                alt="Innexgo Logo"
                src={innexgo_logo}
                width="30"
                height="30"
                className="d-inline-block align-top"
              />{' '}
                Innexgo</h4>
            <Form>
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
            <p style={{fontSize: '15px', marginTop: '12px'}}>Forgot password? <a href="">Click here</a></p>
        </div>
      </div>
    </ExternalLayout>
  )
}

export default Login;
