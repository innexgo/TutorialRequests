import React from 'react';
import { ArrowForward } from '@material-ui/icons';
import transparent from "../img/innexgo_transparent_icon.png"
import innexgo_logo from '../img/innexgo_transparent_icon.png';
import { Nav, Navbar, Button, Form, } from 'react-bootstrap'

import SchoolName from './SchoolName';
import { fetchApi } from '../utils/utils';

interface LoginProps {
  setApiKey: (data: ApiKey | null) => void
}


function Login(props: LoginProps) {

  const formStyle = {
    width: '95%',
  }

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

  return (
    <div style={{ height: '100vh' }}>
      <div className="row h-100">
        <div className="h-100 px-5 py-5 w-25 text-light" style={{
          backgroundColor: '#990000ff',
        }}>
          <img src={transparent} />
          <h4 className="my-3">Attendance simplified.</h4>
          <ul>
            <li>
              <a href="" className="text-light">
                New? Create an account <ArrowForward />
              </a>
            </li>
            <li>
              <a href="" className="text-light">
                Forgot password?<ArrowForward />
              </a>
            </li>
            <li>
              <a href="" className="text-light">
                Not your school?<ArrowForward />
              </a>
            </li>
            <li>
              <a href="/instructions" className="text-light">
                Instructions <ArrowForward />
              </a>
            </li>
          </ul>
        </div>
        <div className="h-100 px-5 py-5 w-75" >
          <h4><SchoolName /></h4>
          <br />
          <Form style={formStyle}>
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

export default Login;
