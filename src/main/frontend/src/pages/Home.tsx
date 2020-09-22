import React from 'react';
import { Link } from 'react-router-dom';
import { Jumbotron, Container, Row, Button, Form, Col } from 'react-bootstrap';
import ExternalLayout from "../components/ExternalLayout";

import transparent from "../img/innexgo_transparent_icon.png"

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
    const firstStyle = {
      textAlign: 'center' as const,
    };

    const jumboStyle = {
      backgroundImage: "linear-gradient(0deg, rgba(119,120,161,1) 0%, rgba(42,68,140,1) 100%)",
      height: "50vh",
      alignItems: "center",
      backgroundAttachment: "fixed",
      backgroundPosition: "center",
      backgroundRepeat: "no-repeat",
      backgroundSize: "cover",
      display: "flex",
      color: "#fff",
      justifyContent: "center"
    };

    return (
      <ExternalLayout fixed={true} transparentTop={true}>
        <Jumbotron fluid style={jumboStyle}>
          <Container>
            <div style={firstStyle}>
              <img src={transparent} />
              <h4>Attendance simplified.</h4>
              <Button href="/user" variant="light" className="mx-3">Teacher Login</Button>
              <Button href="/student" variant="light" className="mx-3">Student Login</Button>
            </div>
          </Container>
        </Jumbotron>
      </ExternalLayout>
    )
  }
}

export default Home;
