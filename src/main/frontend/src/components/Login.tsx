import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Button, Row, Col, Form, } from 'react-bootstrap'
import { newApiKey, isApiErrorCode } from '../utils/utils';

import SimpleLayout from '../components/SimpleLayout';
import SchoolName from '../components/SchoolName';

interface LoginProps {
  setApiKey: (data: ApiKey | null) => void
}

function LoginForm(props: LoginProps) {

  type LoginValue = {
    email: string,
    password: string,
  }

  const onSubmit = async (values: LoginValue, { setStatus, setErrors }: FormikHelpers<LoginValue>) => {
    // Validate input
    let errors: FormikErrors<LoginValue> = {};
    let hasError = false;
    if (values.email === "") {
      errors.email = "Please enter your email";
      hasError = true;
    }
    if (values.password === "") {
      errors.password = "Please enter your password";
      hasError = true;
    }
    setErrors(errors);
    if (hasError) {
      return;
    }

    const maybeApiKey = await newApiKey({
      userEmail: values.email,
      userPassword: values.password,
      duration: 5 * 60 * 60 * 1000
    });

    if (!isApiErrorCode(maybeApiKey)) {
      // on success set the api key
      props.setApiKey(maybeApiKey);
    } else {
      // otherwise display errors
      switch (maybeApiKey) {
        case "USER_NONEXISTENT": {
          setErrors({
            email: "No such user exists"
          });
          break;
        }
        case "PASSWORD_INCORRECT": {
          setErrors({
            password: "Your password is incorrect"
          });
          break;
        }
        default: {
          setStatus("An unknown or network error has occured while trying to log you in");
          break;
        }
      }
      return;
    }
  }

  return (
    <Formik<LoginValue>
      onSubmit={onSubmit}
      initialStatus=""
      initialValues={{
        email: "",
        password: "",
      }}
    >
      {(props) => (
        <Form
          noValidate
          onSubmit={props.handleSubmit} >
          <Form.Group as={Row} >
            <Form.Label column md={2}>Email</Form.Label>
            <Col md={5}>
              <Form.Control
                name="email"
                type="email"
                placeholder="Email"
                value={props.values.email}
                onChange={props.handleChange}
                isInvalid={!!props.errors.email}
              />
              <Form.Control.Feedback type="invalid"> {props.errors.email} </Form.Control.Feedback>
            </Col>
          </Form.Group>
          <Form.Group as={Row} >
            <Form.Label column md={2}>Password</Form.Label>
            <Col md={5}>
              <Form.Control
                name="password"
                type="password"
                placeholder="Password"
                value={props.values.password}
                onChange={props.handleChange}
                isInvalid={!!props.errors.password}
              />
              <Form.Control.Feedback type="invalid">{props.errors.password}</Form.Control.Feedback>
            </Col>
          </Form.Group>
          <Button type="submit">Login</Button>
          <br />
          <Form.Control.Feedback type="invalid">{props.status}</Form.Control.Feedback>
          <br />
          <Form.Text className="text-muted">
            <a href="/forgotpassword">Forgot Password?</a>
          </Form.Text>
        </Form>
      )}
    </Formik>
  )
}

function Login(props: LoginProps) {
  return (
    <SimpleLayout>
      <div className="px-3 py-3">
        <h4>Login to <SchoolName /></h4>
        <LoginForm {...props} />
      </div>
    </SimpleLayout>
  );
}

export default Login;
