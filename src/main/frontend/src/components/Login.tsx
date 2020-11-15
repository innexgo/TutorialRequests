import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Card, Button, Form, } from 'react-bootstrap'
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
          console.log(maybeApiKey);
          setStatus("An unknown or network error has occured while trying to log you in");
          break;
        }
      }
      return;
    }
  }

  return <>
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
          <Form.Group >
            <Form.Label >Email</Form.Label>
            <Form.Control
              name="email"
              type="email"
              placeholder="Email"
              value={props.values.email}
              onChange={props.handleChange}
              isInvalid={!!props.errors.email}
            />
            <Form.Control.Feedback type="invalid"> {props.errors.email} </Form.Control.Feedback>
          </Form.Group>
          <Form.Group >
            <Form.Label>Password</Form.Label>
            <Form.Control
              name="password"
              type="password"
              placeholder="Password"
              value={props.values.password}
              onChange={props.handleChange}
              isInvalid={!!props.errors.password}
            />
            <Form.Control.Feedback type="invalid">{props.errors.password}</Form.Control.Feedback>
          </Form.Group>
          <br />
          <Button type="submit">Login</Button>
          <br />
          <Form.Text className="text-danger">{props.status}</Form.Text>
          <br />
          <Form.Text className="text-muted">
            <a href="/forgot_password">Forgot Password?</a>
          </Form.Text>
        </Form>
      )}
    </Formik>
  </>
}

function Login(props: LoginProps) {
  return (
    <SimpleLayout>
      <div className="h-100 w-100 d-flex">
        <Card className="mx-auto my-auto">
          <Card.Body>
            <Card.Title>Login to <SchoolName /></Card.Title>
            <LoginForm {...props} />
          </Card.Body>
        </Card>
      </div>
    </SimpleLayout>
  );
}

export default Login;
