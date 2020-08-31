import React from 'react';
import { Button, Card, Form } from 'react-bootstrap'

import ExternalLayout from '../components/ExternalLayout';
import { fetchApi } from '../utils/utils';

import innexgo_logo from '../img/innexgo_logo_dark.png';

import blurred_bg from '../img/homepage-bg.png';



export default function Register() {
  const bgStyle = {
    backgroundImage: `radial-gradient(rgba(0, 0, 0, 0.9),rgba(0, 0, 0, 0.1)), url(${blurred_bg})`,
    height: "100vh",
    alignItems: "center",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    backgroundSize: "cover",
    display: "flex",
    justifyContent: "center"
  };

  const errorStyle = {
    color: "#DC143C"
  }

  const [errorText, setErrorText] = React.useState("");
  const [userName, setUserName] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [code, setCode] = React.useState("");

  async function postRegister() {
  //TODO api call to register
  }

  return (
    <ExternalLayout fixed={false} transparentTop={true}>
      <div style={bgStyle}>
        <Card>
          <Card.Body>
            <Card.Title>
              <h4><img
                alt="Innexgo Logo"
                src={innexgo_logo}
                width="30"
                height="30"
                className="d-inline-block align-top"
              />{' '}
                Innexgo</h4>
            </Card.Title>
            <p>Register</p>
            <Form>
              <Form.Group>
                <Form.Control id="username" type="email" placeholder="Email"
                  onChange={e => {
                    setUserName(e.target.value);
                  }} />
                <br />
                <Form.Control id="password" type="password" placeholder="Password"
                  onChange={e => {
                    setPassword(e.target.value);
                  }} />
                  <br />
                   <Form.Control id="code" type="text" placeholder="District Code"
                  onChange={e => {
                    setCode(e.target.value);
                  }} />
                <p className="form-text text-danger" id="error"></p>
              </Form.Group>
              <Button variant="primary" onClick={async () => postRegister()}>Register</Button>
              <p style={errorStyle}>{errorText}</p>
            </Form>
            <br />
          </Card.Body>
        </Card>
      </div>
    </ExternalLayout>
  )
}

