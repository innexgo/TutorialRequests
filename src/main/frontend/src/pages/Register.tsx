import React from 'react';
import { Button, Card, Form } from 'react-bootstrap'
import { ArrowForward } from '@material-ui/icons';
import transparent from "../img/innexgo_transparent_icon.png"

import ExternalLayout from '../components/ExternalLayout';
import { fetchApi } from '../utils/utils';

import innexgo_logo from '../img/innexgo_logo_dark.png';



export default function Register() {

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
  const [code, setCode] = React.useState("");

  async function postRegister() {
  //TODO api call to register
  }

  return (

    <div style={{ height: '100vh' }}>
      <div className="row h-100">
        <div className="h-100 px-5 py-5 w-25 text-light" style={{
          minWidth: "5rem",
          backgroundColor: '#990000ff',
        }}>
          <img src={transparent} alt="Innexgo Logo" />
          <h4 className="my-3">Attendance simplified.</h4>
          <p>To use Innexgo Hours, your school/district has to have a subcription. 
            To learn more, click <a href="https://hours.innexgo.com" style={{
              color: 'white',
              textDecoration: 'underline',
            }}>here</a>.
          </p>
        </div>
        <div className="h-100 px-5 py-5 w-75" >
          <Form>
          <Form.Group>
            <h4 style={{paddingBottom: '20px'}}>Create an account (teachers)</h4>
            <Form.Control style={formBoxStyle} id="username" type="email" placeholder="Email"/>
            <p style={{color:'#484445'}}>Use the email associated with your school/district.</p>
            
            <Form.Control style={formBoxStyle} id="password" type="password" placeholder="Password"/>
            <p style={{color:'#484445'}}>Choose a strong password you'll be able to remember.</p>

            <Form.Control style={formBoxStyle} id="code" type="code" placeholder="District Code"/>
            <p style={{color:'#484445'}}>This is a code from your district to verify your identity and connect you to your school.</p>
            <p className="form-text text-danger" id="error"></p>
          </Form.Group>
          <Button variant="dark">Register</Button>
          <p>{/*error stuff*/}</p>
        </Form>
        </div>
      </div>
    </div>
  )
}

