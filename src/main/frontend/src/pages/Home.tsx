import React from 'react';
import { Link } from 'react-router-dom';
import { Jumbotron, Container, Row, Button, Form, Col } from 'react-bootstrap';
import ExternalLayout from "../components/ExternalLayout";

import transparent from "../img/innexgo_logo_dark.png"

interface Props { };
interface State {
  windowWidth: number;
  windowHeight: number;
};

class Home extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      windowWidth: 0,
      windowHeight: 0,
    }
    this.updateDimensions = this.updateDimensions.bind(this);
  }

  componentDidMount() {
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
  };

  render() {
    const jumbotronStyle = {
      marginTop: '15px'
    } as React.CSSProperties;

    const halfColumnOne = {
      float: 'left',
      width: '50%',
      alignItems: 'left'
    } as React.CSSProperties;

    const halfColumnTwo = {
      float: 'left',
      width: '50%',
      textAlign: 'left'
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

    /*in login form these are things it lacks:
    -in Form.Control, onchange functions
    -in login Button, onClick postlogin function
    -in error text, removed errorStyle and errorText

    -apikey
    -errorText, username, password set__ functions with React.useState("")
    -async function
    */

    return (
      <ExternalLayout fixed={false} transparentTop={true}>
        <Container style={jumbotronStyle}>
          <div style={halfColumnOne}>
            <div style={{marginLeft: '5px'}}>
              <img src={transparent} style={{marginBottom: '7px'}}/>
              <h4>Attendance simplified.</h4>
            </div>
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

            {/*<p style={{fontSize: '15px', marginTop: '12px'}}>Forgot password? <a href="">Click here</a></p>
            <p style={{fontSize: '15px', marginTop: '5px'}}>Or, <a href="">create an account</a></p>
            */}
            </div>

          <div style={halfColumnTwo}>

          </div>
        </Container>
        <div style={{ /*clears rows after, similar to row:after css*/
          content: "",
          display: 'table',
          clear: 'both'
        }}/>

      </ExternalLayout>
    )
  }
}

export default Home;
