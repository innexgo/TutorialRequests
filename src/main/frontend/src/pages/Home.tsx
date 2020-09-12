import React from 'react';
import {Jumbotron, Container, Row, Button, Form, Col} from 'react-bootstrap';
/*import {Event, DirectionsRunOutlined, Schedule} from '@material-ui/icons';*/
import AOS from "aos";
import "aos/dist/aos.css";

import ExternalLayout from "../components/ExternalLayout";

import heroBg from "../img/homepage-bg.png";
import kids_and_books from "../img/kids_and_books.png";
import deal from "../img/deal.png";
import contact from "../img/contact.png"

interface Props{};
interface State{
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
    AOS.init({
      duration : 2000
    });
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
    const windowWidth = this.state.windowWidth;
    const tablet = windowWidth < 934;

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

    const secondColumn = {
      float: tablet ? 'initial' : 'left',
      width: tablet ? '100%' : '50%',
      margin: tablet ? 'auto' : '10px auto',
    } as React.CSSProperties;

    return (
      <ExternalLayout fixed={true} transparentTop={true}>
        <Jumbotron fluid style={jumboStyle}>
          <Container>
            <h1> Academics, Achievement, Attendance first. </h1>
          </Container>
        </Jumbotron>
        <section style={{boxSizing: 'border-box'}}>
          <Container>
            <Row>
              <div>
                <h2 style={{textAlign:'center', margin: '25px auto',}} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">What is Innexgo Hours?</h2>
                
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                  <p>Innexgo Hours is a service that helps teachers and students create and organize office hour appointments for all of their classes. 
                    Using a simple portal and calendar system, students can easily seek help from their teachers, while teachers and adminstrators can track the time students are spending in office hours.</p>
                
                  <p>Offering teachers and students the oppurtunity to participate in office hours ensures students' success with individualized help,
                    and gives teachers the ability to focus on one student at a time. Innexgo Hours helps schools provide this additional method of support
                    without the hassle of organizing several courses with hundreds of students, 
                    dealing with extra emails, or paying high prices for limited features.
                  </p>
                </div>

                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                  <img src={kids_and_books}/>
                </div>
              </div>
            </Row>
          </Container>
          <Container>

            <Row>
              <div>
                <div style={secondColumn}></div>
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                  <h5>Organized calendar</h5>
                  <p>Teachers and students can see all of their office hour appointments in a simple
                    and clear calendar, private to them and showing only their own appointments.
                  </p>
                </div>
              </div>
            </Row>
            <Row>
              <div>
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                  <h5>Student logins</h5>
                  <p>Students are able to log into their own Innexgo Hours portal and see all
                    of their office hour appointments in one place without having to remember several
                    logins for different office hour applications.
                  </p>
                </div>
                <div style={secondColumn}></div>
              </div>
            </Row>
            <Row>
              <div>
                <div style={secondColumn}></div>
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                  <h5>Time Reports</h5>
                  <p>Time that students spend in office hours is tracked to provide teachers and school 
                    administrators helpful analytics that can be used to track attendance, as well as gauge 
                    how students are performing and what type of help they need.
                    </p>
                </div>
              </div>
            </Row>
            <hr />
            <Row>
              <div style={{
                float: tablet ? 'initial' : 'left',
                width: tablet ? '100%' : '65%',
                margin: tablet ? 'auto' : '10px auto',
              }} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                <h2 style={{margin: '15px auto'}}>Pricing</h2>
                <p>
                  Innexgo Hours charges a consistent price for all schools, <b>$1 per student, per year</b>.
                  This covers an account for as many teachers as needed, every single student, tech support,
                  as well as our promise to always improve Hours for you. Contracts with individual schools 
                  may vary; please contact us for more detail.
                  </p>
                  <a href="#contact"><Button variant="light">Contact us</Button></a>
              </div>
              <div style={{
                float: tablet ? 'initial' : 'left',
                width: tablet ? '100%' : '35%',
                margin: tablet ? 'auto' : '10px auto',
              }}>
                <img src={deal} style={{width: '100%', height: '100%'}}/>
              </div>
            </Row>
            <hr/>
            <Row data-aos="fade-down" data-aos-duration="2400" data-aos-once="true">
              <h2 style={{textAlign: 'center', margin: '15px auto'}}>Frequently Asked Questions</h2>

              <p style={{fontSize: '20px', margin: '5px auto'}}>
                What information does Innexgo Hours collect for schools?
                </p>
              <p>Innexgo Hours tracks how long students are spending in tutorials and how long
                teachers are giving them. This may be helpful for attendance purposes and/or understanding
                how students are learning best in your school.
              </p>

              <p style={{fontSize: '20px', margin: '5px auto'}}>
                Does Innexgo Hours allow students to make appointments with multiple teachers?
                </p>
              <p>Yes! Students can see all teachers in one place and make appointments from their portal.
                Keeping all classes in one place ensures that students don't double book and are able to see
                their schedule in one place.
              </p>

              <p style={{fontSize: '20px', margin: '5px auto'}}>
                Why shoud I use Innexgo Hours over other services?
                </p>
              <p>Innexgo Hours was designed specifically for schools in mind. Unlike other websites, which
                are only designed for a small group of people, or calendars that give all users editing privileges,
                Innexgo Hours is secure and does the organizing work teachers would usually have to do.
                Pricing for Innexgo Hours is also significantly lower than other services with less features.
              </p>
            </Row>
              <hr/>
            <Row>
              <div id="contact">
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2400" data-aos-once="true"> 
                  <div style={{padding: '2px 15px'}}>
                    <img src={contact} style={{width: '100%', height: '100%'}}/>
                  </div>
                </div>

                {/*!!!!!!CONTACT FORM NOT FUNCTIONAL---------------------*/}
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2400" data-aos-once="true">
                <h2 style={{marginBottom: '15px', marginTop: '-3px', marginLeft: "-2px"}}>Contact Us</h2>
                <p>Interested in Innexgo Hours, or have a question?
                  Send us a message and we'll get back to you as soon as possible!</p>
                <Form style={{padding: '2px 10px'}}>
                  <Form.Group as={Row} controlId="name">
                    <Form.Label column sm={2}>Name</Form.Label>
                    <Col sm={10}>
                      <Form.Control type="text" placeholder="Name"/>
                    </Col>
                  </Form.Group> <br/>

                  <Form.Group as={Row} controlId="email">
                    <Form.Label column sm={2}>Email</Form.Label>
                    <Col sm={10}>
                      <Form.Control type="email" placeholder="Enter email" />
                    </Col>
                  </Form.Group> <br/>

                  <Form.Group as={Row} controlId="position">
                    <Form.Label column sm={2}>Position</Form.Label>
                    <Col sm={10}>
                      <Form.Control as="select" placeholder="" custom style={{
                      padding:'3px', height: '40px'}}>
                        <option value="district">District Administration</option>
                        <option value="school">School Administration</option>
                        <option value="other">Other</option>
                      </Form.Control>
                    </Col>
                  </Form.Group> <br/>

                  <Form.Group as={Row} controlId="message">
                    <Form.Label column sm={2}>Message</Form.Label>
                    <Col sm={10}>
                      <Form.Control type="text" placeholder="Type your message here"/>
                    </Col>
                  </Form.Group> <br/>

                  <Button variant="light" type="submit">
                    Submit
                  </Button>
                </Form>
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
