import React from 'react';
import { ArrowForward } from '@material-ui/icons';
import transparent from "../img/innexgo_transparent_icon.png"
import innexgo_logo from '../img/innexgo_transparent_icon.png';
import { Nav, Navbar, Button, Form, } from 'react-bootstrap'

function Home() {

  const formStyle = {
    width: '95%',
  }

  const formBoxStyle = {
    borderLeft: '0',
    borderTop: '0',
    borderRight: '0',
    borderRadius: '0',
    borderBottom: '1px solid grey',
  };

  /*in login form these are things it lacks:
  -in Form.Control, onchange functions
  -in login Button, onClick postlogin function
  -in error text, removed errorStyle and errorText

  -apikey
  -errorText, username, password set__ functions with React.useState("")
  -async function
  */

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
          <h4>Squidward Community College</h4> <br />
          <Form style={formStyle}>
            <Form.Group>
              <Form.Control style={formBoxStyle} id="username" type="email" placeholder="Email" />
              <br />
              <Form.Control style={formBoxStyle} id="password" type="password" placeholder="Password" />
              <p className="form-text text-danger" id="error"></p>
            </Form.Group>
            <Button variant="dark">Login</Button>
            <p></p> {/*error text*/}
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

export default Home;
