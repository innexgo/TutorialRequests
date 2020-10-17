import React from 'react';
import { ArrowForward } from '@material-ui/icons';
import transparent from "../img/innexgo_transparent_icon.png"
import innexgo_logo from '../img/innexgo_transparent_icon.png';
import { Nav, Navbar, } from 'react-bootstrap'
import Login from '../components/Login';

import SchoolName from './SchoolName';

interface LoginInterfaceProps {
  setApiKey: (a:ApiKey|null) => void
}

function LoginInterface(props: LoginInterfaceProps) {
  return (
    <div style={{ height: '100vh' }}>
      <div className="row h-100">
        <div className="h-100 px-5 py-5 w-25 text-light" style={{
          minWidth: "5rem",
          backgroundColor: '#990000ff',
        }}>
          <img src={transparent} />
          <h4 className="my-3">Attendance simplified.</h4>
          <a href="" className="text-light">
            New? Create an account <ArrowForward />
          </a>
          <br />
          <a href="" className="text-light">
            Forgot password?<ArrowForward />
          </a>
          <br />
          <a href="" className="text-light">
            Not your school?<ArrowForward />
          </a>
          <br />
          <a href="/instructions" className="text-light">
            Instructions<ArrowForward />
          </a>
        </div>
        <div className="h-100 px-5 py-5 w-75" >
          <h4><SchoolName /></h4>
          <br />
          <Login setApiKey={props.setApiKey}/>
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

export default LoginInterface;
