import React from 'react';
/*import { Link } from 'react-router-dom';*/
import transparent from "../img/innexgo_transparent_icon.png"
import innexgo_logo from '../img/innexgo_transparent_icon.png';
import { Nav, Navbar, Button, Form, } from 'react-bootstrap'

interface Props { };
interface State {
  /*windowWidth: number;
  windowHeight: number;*/
};

class Home extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    /*this.state = {
      windowWidth: 0,
      windowHeight: 0,
    }*/
    /*this.updateDimensions = this.updateDimensions.bind(this);*/
  }

  /*componentDidMount() {
    this.updateDimensions();
    window.addEventListener("resize", this.updateDimensions);

  }

  componentWillUnmount() {
    window.removeEventListener("resize", this.updateDimensions);
  }

  updateDimensions() {
    let windowWidth = typeof window !== "undefined" ? window.innerWidth : 0;
    let windowHeight = typeof window !== "undefined" ? window.innerHeight : 0;

    this.setState({ windowWidth, windowHeight });
  };*/

  mouseOver(e: any) {
    e.target.style.textDecoration = 'underline';
    e.target.style.color = '#fffff2';
  }

  mouseLeave(e: any) {
    e.target.style.textDecoration = 'none';
    e.target.style.color = 'white';
  }


  render() {

    const columnOne = {
      float: 'left',
      width: '30%',
      alignItems: 'left',
      backgroundColor: '#990000ff',
      margin: '0',
      padding: '3%',
      color: 'white',
      height: '100%'
    } as React.CSSProperties;

    const columnTwo = {
      float: 'left',
      width: '70%',
      textAlign: 'left',
      margin: '0',
      padding: '6%',
      position: 'relative',
      height: '100%',
    } as React.CSSProperties;

    const formStyle = {
      width: '95%',
    } as React.CSSProperties;

    const formBoxStyle={
      borderLeft:'0',
      borderTop:'0',
      borderRight:'0',
      borderRadius: '0',
      borderBottom: '1px solid grey',
    };

    const linkStyle = {
      marginBottom: '3px', 
      fontSize: '14px',
      textDecoration: 'none',
      color: 'white',
    } as React.CSSProperties;

    /*in login form these are things it lacks:
    -in Form.Control, onchange functions
    -in login Button, onClick postlogin function
    -in error text, removed errorStyle and errorText

    -apikey
    -errorText, username, password set__ functions with React.useState("")
    -async function
    */

    return (
      <div>

        <div style={{height: '97vh'}}>
          <div style={columnOne}>
            <img src={transparent} style={{height: 'auto', width: 'auto'}}/>
            <h4 style={{marginTop: '10px', marginBottom: '69px'}}>Attendance simplified.</h4>
            <a href="" style={linkStyle} onMouseOver={this.mouseOver} onMouseLeave={this.mouseLeave}>
              New? Create an account &#8594;</a> <br/>
            <a href="" style={linkStyle} onMouseOver={this.mouseOver} onMouseLeave={this.mouseLeave}>
              Forgot password? &#8594;</a> <br/>
            <a href="" style={linkStyle} onMouseOver={this.mouseOver} onMouseLeave={this.mouseLeave}>
              Not your school? &#8594;</a> <br/>
            
          </div>

          <div style={columnTwo}>
            <h4>Squidward Community College</h4> <br/>
            <Form style={formStyle}>
              <Form.Group>
                <Form.Control style={formBoxStyle} id="username" type="email" placeholder="Email"/>
                <br />
                <Form.Control style={formBoxStyle} id="password" type="password" placeholder="Password"/>
                <p className="form-text text-danger" id="error"></p>
              </Form.Group>
              <Button variant="dark">Login</Button>
              <p></p> {/*error text*/}
            </Form>
          </div>
        </div>

        <div style={{content: "",
          display: 'table',
          clear: 'both'}}></div>

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
}

export default Home;
