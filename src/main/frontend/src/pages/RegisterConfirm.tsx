import React from 'react';
import { Button, Card, Form } from 'react-bootstrap'

import ExternalLayout from '../components/ExternalLayout';
import { fetchApi } from '../utils/utils';

import innexgo_logo from '../img/innexgo_logo_dark.png';

import blurred_bg from '../img/homepage-bg.png';



function RegisterConfirm() {
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

  
  //TODO get api call, verify email, put either success or error in this variable:
    const result = ""
  

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
            <p>{result}</p>
            <br />
          </Card.Body>
        </Card>
      </div>
    </ExternalLayout>
  )
}

export default RegisterConfirm;
