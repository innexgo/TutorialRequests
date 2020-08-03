import React from 'react';
import { Button, Card, Form } from 'react-bootstrap'

import ExternalLayout from '../components/ExternalLayout';
import { fetchApi } from '../utils/utils';

import innexgo_logo from '../img/innexgo_logo_dark.png';

import blurred_bg from '../img/homepage-bg.png';

interface StudentLoginProps {
  setStudent: (student: Student | null) => void
}

function StudentLogin({ setStudent }: StudentLoginProps) {
  const bgStyle = {
    backgroundImage: `radial-gradient(rgba(0, 0, 0, 0.9),rgba(0, 0, 0, 0.1)), url(${blurred_bg})`,
    height: "100vh",
    alignItems: "center",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    backgroundSize: "cover",
    display: "flex",
    justifyContent: "center"
  };

  const errorStyle = {
    color: "#DC143C"
  }

  const [errorText, setErrorText] = React.useState("");
  const [id, setId] = React.useState("");

  async function postStudentLogin() {

    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/
    // DEVELOPER ONLY TODO TODO don't do this in production
    setStudent({
      id: 0,
      name: "Stewart McStudent"
    } as Student);
    return;
    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/

    try {
      const student = await fetchApi(`misc/validateStudentId/?` + new URLSearchParams([
        ['studentId', id],
      ])) as Student;
      setStudent(student);
    } catch (e) {
      console.log(e);
      setErrorText("Your id did not match our records");
    }
  }

  return (
    <ExternalLayout fixed={false} transparentTop={true}>
      <div style={bgStyle}>
        <Card>
          <Card.Body>
            <Card.Title>
              <h4><img
                alt="Innexgo Logo"
                src={innexgo_logo}
                width="30"
                height="30"
                className="d-inline-block align-top"
              />{' '}
                Innexgo</h4>
            </Card.Title>
            <p>Login to Dashboard (Student)</p>
            <Form>
              <Form.Group>
                <Form.Control id="id" type="number" placeholder="Student ID"
                  onChange={e => {
                    setId(e.target.value);
                  }} />
                <br />
                <p className="form-text text-danger" id="error"></p>
              </Form.Group>
              <Button variant="primary" onClick={async () => postStudentLogin()}>Login as Student</Button>
              <p style={errorStyle}>{errorText}</p>
            </Form>
            <br />
          </Card.Body>
        </Card>
      </div>
    </ExternalLayout>
  )
}

export default StudentLogin;
