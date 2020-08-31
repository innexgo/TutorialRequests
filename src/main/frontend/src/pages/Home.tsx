import React from 'react';
import {Jumbotron, Container, Row, Card, CardDeck } from 'react-bootstrap';
import { VerifiedUser, BarChart, ThumbUp} from '@material-ui/icons'
import AOS from 'aos';
import 'aos/dist/aos.css';

import ExternalLayout from "../components/ExternalLayout";

import heroBg from "../img/homepage-bg.png"

interface Props{};
interface State{};

class Home extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    AOS.init();
  }

  render() {
    const jumboStyle = {
      backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.5),rgba(0, 0, 0, 0.3)), url(${heroBg})`,
      height: "100vh",
      alignItems: "center",
      backgroundAttachment: "fixed",
      backgroundPosition: "center",
      backgroundRepeat: "no-repeat",
      backgroundSize: "cover",
      display: "flex",
      color: "#fff",
      justifyContent: "center"
    };

    const iconStyle = {
      width: "120px",
      height: "120px",
      display: "inline-block",
      borderRadius: "40px",
      color: "#fefefe",
      background: "#990000ff",
      padding: "20px",
      margin: "10px"
    };

    const testimonialItemStyle = {
      display: "inline-block",
      padding: "0",
      position: "relative" as const,
      marginTop: "25px",
      marginBottom: "15px",
      width: "100%"
    };

    const testimonialItemOccupationStyle = {
      color: "#aaa",
      display: "block",
      position: "relative" as const,
    }

    const testimonalItemAuthorStyle = {
      display: "block",
      color: "#444",
      fontWeight: "bold" as const,
      marginTop: "20px",
    };

    const thirdColumn = { 
      float: 'left',
      width: '33%',
      textAlign: 'center',
    } as React.CSSProperties;


    return (
      <ExternalLayout fixed={true} transparentTop={true}>
        <Jumbotron fluid style={jumboStyle}>
          <Container>
            <h1> Academics, Achievement, Attendance first. </h1>
          </Container>
        </Jumbotron>
        <section>
          <Container>
            <Row>
              <div style={thirdColumn} data-aos="fade-up" data-aos-duration="2000" data-aos-once="true">
                <ThumbUp style={iconStyle}/>
                <h5>Easy to Use</h5>
                <p>Increases teaching time by automating attendance in every classroom and decreasing teacher responsibilities. </p>
              </div>
              <div style={thirdColumn} data-aos="fade-up" data-aos-duration="2300" data-aos-once="true">
                <VerifiedUser style={iconStyle} />
                <h5>Secure Campus</h5>
                <p>Ensures schoolwide safety by recording student entrances and exits and preventing chronic absenteeism in integrated classrooms. </p>
              </div>
              <div style={thirdColumn} data-aos="fade-up" data-aos-duration="2500" data-aos-once="true">
                <BarChart style={iconStyle}/>
                <h5>Detailed Reporting</h5>
                <p> Analyzes attendance data to provide extensive administrator reports on in-session campus safety and attendance. </p>
              </div>
            </Row>
            <hr />
            <Row>
              <h2>Our Strategy</h2>
              <CardDeck>
                <Card>
                  <Card.Body data-aos="fade-left" data-aos-duration="2200" data-aos-once="true">
                    <Card.Title> Data Collection  </Card.Title>
                    <Card.Text>
                      The process begins with our RFID technology. All classrooms will have a scanner that captures scan-in/out data
                      from students whenever an ID card is detected.
                    </Card.Text>
                  </Card.Body>
                </Card>
                <Card>
                  <Card.Body data-aos="fade-left" data-aos-duration="2400" data-aos-once="true">
                    <Card.Title> Data Processing </Card.Title>
                    <Card.Text>
                      Data from the RFID-driven scanners are transmitted to the Innexgo database
                      where this data is sorted into categorizations such as class periods, classrooms, and teachers.
                    </Card.Text>
                  </Card.Body>
                </Card>
                <Card>
                  <Card.Body data-aos="fade-left" data-aos-duration="2600" data-aos-once="true">
                    <Card.Title>Data Analysis</Card.Title>
                    <Card.Text>
                      Innexgo displays the attendance data through our analytics dashboard where
                      teachers and administrators can monitor student attendance records and access numerous charts and reports.
                    </Card.Text>
                  </Card.Body>
                </Card>
              </CardDeck>
            </Row>
            <hr />
            <Row>
              <h2>What people say about us</h2>
              <div style={{marginLeft: "1%"}}>
                <div style={testimonialItemStyle} data-aos="fade-up" data-aos-duration="2000" data-aos-once="true">
                  <p>&quot;Less time on trying to check who&apos;s there and more time for teaching.&quot;</p>
                  <span style={testimonalItemAuthorStyle}>Channy Cornejo</span>
                  <span style={testimonialItemOccupationStyle}>Math Department Chair</span>
                </div>
                <div style={testimonialItemStyle} data-aos="fade-up" data-aos-duration="2200" data-aos-once="true">
                  <p>&quot;It holds students accountable for their attendance habits.&quot;</p>
                  <span style={testimonalItemAuthorStyle}>Carole Ng</span>
                  <span style={testimonialItemOccupationStyle}>Computer Science Teacher</span>
                  <br/><br/><br/>
                </div>
              </div>
            </Row>
          </Container>
        </section>
      </ExternalLayout>
    )
  }
}

export default Home;
