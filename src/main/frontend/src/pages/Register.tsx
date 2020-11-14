import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Button, Row, Col, Form, } from 'react-bootstrap'

import { newEmailVerificationChallenge, isApiErrorCode } from '../utils/utils';

import SimpleLayout from '../components/SimpleLayout';
import SchoolName from '../components/SchoolName';

function RegisterForm() {

  type RegistrationValue = {
    firstName: string,
    lastName: string,
    email: string,
    password1: string,
    password2: string,
    terms: boolean,
  }

  const isPasswordValid = (pass: string) => pass.length >= 8 && /\d/.test(pass);

  const onSubmit = async (values: RegistrationValue, props: FormikHelpers<RegistrationValue>) => {
    // Validate input
    let errors: FormikErrors<RegistrationValue> = {};
    let hasError = false;
    if (values.firstName === "") {
      errors.firstName = "Please enter your first name";
      hasError = true;
    }
    if (values.lastName === "") {
      errors.lastName = "Please enter your last name";
      hasError = true;
    }
    if (values.email === "") {
      errors.email = "Please enter your email";
      hasError = true;
    }
    if (!isPasswordValid(values.password1)) {
      errors.password1 = "Password must have at least 8 chars and 1 number";
      hasError = true;
    }
    if (values.password2 !== values.password1) {
      errors.password2 = "Password does not match";
      hasError = true;
    }
    if (!values.terms) {
      errors.terms = "You must agree to the terms and conditions";
      hasError = true;
    }
    props.setErrors(errors);
    if (hasError) {
      return;
    }

    const maybeEmailVerificationChallenge = newEmailVerificationChallenge({
      userName: `${values.firstName.trim()} ${values.lastName.trim()}`,
      userEmail: values.email,
      userPassword: values.password1,
      userKind: "STUDENT"
    });

    if (!isApiErrorCode(maybeEmailVerificationChallenge)) {
      // On success set status to successful
      props.setStatus("Success! Check your email to continue the registration process.");
    } else {
      // otherwise display errors
      switch (maybeEmailVerificationChallenge) {
        case "USER_EMAIL_EMPTY": {
          props.setErrors({
            email: "No such user exists"
          });
          break;
        }
        case "USER_NAME_EMPTY": {
          props.setErrors({
            firstName: "Please enter your first name",
            lastName: "Please enter your last name"
          });
          break;
        }
        case "USER_EXISTENT": {
          props.setErrors({
            email: "A user with this email already exists."
          });
          break;
        }
        case "PASSWORD_INSECURE": {
          props.setErrors({
            password1: "Password is of insufficient complexity"
          });
          break;
        }
        case "EMAIL_RATELIMIT": {
          props.setErrors({
            email: "Please wait 5 minutes before sending another email."
          });
          break;
        }
        case "EMAIL_BLACKLISTED": {
          props.setErrors({
            email: "This email address has been blacklisted."
          });
          break;
        }
        default: {
          props.setStatus({
            failureMessage: "An unknown or network error has occured while trying to log you in",
            successMessage: ""
          });
          break;
        }
      }
      return;
    }
    props.setStatus({
      failureMessage: "",
      successMessage: "We've sent an email to verify your address."
    });
  }

  return (
    <Formik
      onSubmit={onSubmit}
      initialStatus={{
        failureMessage: "",
        successMessage: "",
      }}
      initialValues={{
        firstName: "",
        lastName: "",
        email: "",
        password1: "",
        password2: "",
        terms: false,
      }}
    >
      {(props) => (
        <Form
          noValidate
          onSubmit={props.handleSubmit} >
          <Form.Group as={Row} >
            <Form.Label column md={2}>First name</Form.Label>
            <Col md={5}>
              <Form.Control
                name="firstName"
                type="text"
                placeholder="First Name"
                value={props.values.firstName}
                onChange={props.handleChange}
                isInvalid={!!props.errors.firstName}
              />
              <Form.Control.Feedback type="invalid">{props.errors.firstName}</Form.Control.Feedback>
            </Col>
          </Form.Group>
          <Form.Group as={Row} >
            <Form.Label column md={2}>Last name</Form.Label>
            <Col md={5}>
              <Form.Control
                name="lastName"
                type="text"
                placeholder="Last Name"
                value={props.values.lastName}
                onChange={props.handleChange}
                isInvalid={!!props.errors.lastName}
              />
              <Form.Control.Feedback type="invalid">{props.errors.lastName}</Form.Control.Feedback>
            </Col>
          </Form.Group>
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
                name="password1"
                type="password"
                placeholder="Password"
                value={props.values.password1}
                onChange={props.handleChange}
                isInvalid={!!props.errors.password1}
              />
              <Form.Control.Feedback type="invalid">{props.errors.password1}</Form.Control.Feedback>
            </Col>
          </Form.Group>
          <Form.Group as={Row} >
            <Form.Label column md={2}>Confirm Password</Form.Label>
            <Col md={5}>
              <Form.Control
                name="password2"
                type="password"
                placeholder="Confirm Password"
                value={props.values.password2}
                onChange={props.handleChange}
                isInvalid={!!props.errors.password2}
              />
              <Form.Control.Feedback type="invalid">{props.errors.password2}</Form.Control.Feedback>
            </Col>
          </Form.Group>
          <Form.Group>
            <Form.Check
              name="terms"
              required
              label="Agree to terms and conditions"
              onChange={props.handleChange}
              isInvalid={!!props.errors.terms}
              feedback={props.errors.terms}
            />
          </Form.Group>
          <Button type="submit">Submit form</Button>
          <br />
          <Form.Control.Feedback type="invalid">{props.status.failureMessage}</Form.Control.Feedback>
          <Form.Control.Feedback>{props.status.successMessage}</Form.Control.Feedback>
        </Form>
      )}
    </Formik>
  );
}


function Register() {
  return (
    <SimpleLayout>
      <div className="px-3 py-3">
        <h4>Register with <SchoolName /></h4>
        <RegisterForm />
      </div>
    </SimpleLayout>
  )
}

export default Register;
