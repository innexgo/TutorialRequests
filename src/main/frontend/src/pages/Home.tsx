import React from 'react';
import {Jumbotron, Container, Row, Card, CardDeck } from 'react-bootstrap';
import {Event, DirectionsRunOutlined, Schedule} from '@material-ui/icons';
import AOS from "aos";
import "aos/dist/aos.css";

import ExternalLayout from "../components/ExternalLayout";

import heroBg from "../img/homepage-bg.png"
import kids_and_books from "../img/kids_and_books.png"

interface Props{};
interface State{
  one: boolean,
};

class Home extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      one: false
    }
  }

  componentDidMount() {
    AOS.init({
      duration : 2000
    });
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

    const secondColumn = {
      float: 'left',
      width: '50%',
      margin: '10px auto'
    } as React.CSSProperties;

    const thirdColumn = { 
      float: 'left',
      width: '33%',
      textAlign: 'center',
    } as React.CSSProperties;

    const buttonStyle = {
      float: 'right',
      border: 'none',
    } as React.CSSProperties;

    const answerStyle1 = {
      display: this.state.one ? 'initial' : 'none',
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
                <h2 style={{textAlign:'center', margin: '25px auto',}}>What is Innexgo Hours?</h2>
                
                <div style={secondColumn} data-aos="fade-down" data-aos-duration="2000" data-aos-once="true">
                  <p>Innexgo Hours is a service that helps teachers and students create and organize office hour appointments for all of their classes. 
                    Using a simple portal and calendar system, students can easily seek help from their teachers, while teachers and adminstrators can track the time students are spending in office hours.</p>
                
                  <p>Offering teachers and students the oppurtunity to participate in office hours allows students to ensure success with individualized help,
                    teachers to focus on one student instead of an entire class, and schools to provide a better support system for its members.
                    Innexgo Hours helps schools with this, taking out the work of organizing office hours.
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
              <div style={thirdColumn} data-aos="fade-up" data-aos-duration="2000" data-aos-once="true">
                <Event style={iconStyle}/>
                <h5>Calendar</h5>
                <p>Innexgo Hours provides a calenadr for teachers to easily view and organize office hour appointments with students.</p>
              </div>
              <div style={thirdColumn} data-aos="fade-up" data-aos-duration="2300" data-aos-once="true">
                <DirectionsRunOutlined style={iconStyle} />
                <h5>Student Logins</h5>
                <p>Innexgo Hours also gives students logins to easily organize their tutorial appointments with their teachers, without collecting too much data. </p>
              </div>
              <div style={thirdColumn} data-aos="fade-up" data-aos-duration="2500" data-aos-once="true">
                <Schedule style={iconStyle}/>
                <h5>Time Reports</h5>
                <p>Time that students spend in office hours is tracked to provide teachers and school administrators another perspective on students' performance.</p>
              </div>
            </Row>
            <hr />
            <Row>
              <div>
                <button style={buttonStyle} onClick={() => this.setState({one: !this.state.one})} value='+'/>
                <p style={{float: 'left'}}>question text</p>
              </div>
              <p style={answerStyle1}>Answer</p>
            
            </Row>
            <hr/>
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
