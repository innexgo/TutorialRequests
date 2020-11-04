import React from 'react';
import { ArrowForward } from '@material-ui/icons';
import transparent from "../img/innexgo_transparent_icon.png"
import innexgo_logo from '../img/innexgo_transparent_icon.png';
import { Button, Form, Nav, Navbar, } from 'react-bootstrap'
import { fetchApi } from '../utils/utils';

import SchoolName from '../components/SchoolName';

function RegisterForm() {
  const formBoxStyle = {
    borderLeft: '0',
    borderTop: '0',
    borderRight: '0',
    borderRadius: '0',
    borderBottom: '1px solid grey',
  };

  const [validated, setValidated] = React.useState(false);
  const [email, setEmail] = React.useState("");
  const [name, setName] = React.useState("");
  const [password1, setPassword1] = React.useState("");
  const [password2, setPassword2] = React.useState("");


  async function postRegister() {
    /*
    try {
      const apiKey = await fetchApi(`apiKey/new/?` + new URLSearchParams([
        ['duration', `${5 * 60 * 60 * 1000}`], // 5 hours
      ])) as ApiKey;
    } catch (e) {
      console.log(e);
      setErrorText("Your username or password is incorrect");
    }
    */
  }

  function handleSubmit() {
      /*
      if(name != '') {
        setErrorText("Name must be filled.");
        return false;
      }
      if(email != '') {
        setErrorText("Email must be filled.");
        return false;
      }
      if(password1 != '') {
        setErrorText("Password must be filled.");
        return false;
      }
      if(password1 != password2) {
        setErrorText("Password and password confirmation don't match.");
        return false;
      }
      return true;
      */
  }

  return <Form onSubmit={handleSubmit}>
    <Form.Group>
      <Form.Control style={formBoxStyle} placeholder="Email"
        onChange={e => {
          setName(e.target.value);
        }} />
      <Form.Control.Feedback type="invalid">
         Please enter your real name.
      </Form.Control.Feedback>
    </Form.Group>
    <Form.Group>
      <Form.Control style={formBoxStyle} type="email" placeholder="Email"
        onChange={e => {
          setEmail(e.target.value);
        }} />
      <Form.Control.Feedback type="invalid">
         Please enter your school email address.
      </Form.Control.Feedback>
    </Form.Group>
    <Form.Group>
      <Form.Control style={formBoxStyle} type="password" placeholder="Password"
        onChange={e => {
          setPassword1(e.target.value);
        }} />
      <Form.Control.Feedback type="invalid">
         Please enter your school email address.
      </Form.Control.Feedback>
    </Form.Group>
    <Form.Group>
      <Form.Control style={formBoxStyle} type="password" placeholder="Confirm Password"
        onChange={e => {
          setPassword2(e.target.value);
        }} />
      <Form.Control.Feedback type="invalid">
         Please enter your school email address.
      </Form.Control.Feedback>
    </Form.Group>
    <Button type="submit" variant="dark">Register</Button>
  </Form>
}


function Register() {
  return (
    <div style={{ height: '100vh' }}>
      <div className="row h-100">
        <div className="h-100 px-5 py-5 w-25 text-light" style={{
          minWidth: "5rem",
          backgroundColor: '#990000ff',
        }}>
          <img src={transparent} alt="Innexgo Logo" />
          <h4 className="my-3">Attendance simplified.</h4>
          <a href="/" className="text-light">
            Already have an account?<ArrowForward />
          </a>
          <br />
          <a href="" className="text-light">
            Forgot password?<ArrowForward />
          </a>
          <br />
          <a href="https://hours.innexgo.com" className="text-light">
            Not your school?<ArrowForward />
          </a>
          <br />
          <a href="/instructions" className="text-light">
            Instructions<ArrowForward />
          </a>
        </div>
        <div className="h-100 px-5 py-5 w-75" >
          <h4>Register with <SchoolName /></h4>
          <br />
          <RegisterForm/>
        </div>
      </div>

      <Navbar bg="dark" variant="dark">
        <Navbar.Brand href="#home">
          <img
            alt="Innexgo Logo"
            src={innexgo_logo}
            width="30"
            height="30"
            className="d-inline-block align-top"
          />{' '}
              Innexgo
          </Navbar.Brand>
        <Nav>
          <Nav.Link>&copy; Innexgo LLC, 2020</Nav.Link>
          <Nav.Link href="/terms_of_service">Terms of Service</Nav.Link>
          <Nav.Link href="/terms_of_service#cookie_policy">Cookie Policy</Nav.Link>
        </Nav>
      </Navbar>
    </div>
  )
}

export default Register;
