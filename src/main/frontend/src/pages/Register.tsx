import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Button, Card, Form, } from 'react-bootstrap'

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
          <Form.Group >
            <Form.Label >First Name</Form.Label>
            <Form.Control
              name="firstName"
              type="text"
              placeholder="First Name"
              value={props.values.firstName}
              onChange={props.handleChange}
              isInvalid={!!props.errors.firstName}
            />
            <Form.Control.Feedback type="invalid">{props.errors.firstName}</Form.Control.Feedback>
          </Form.Group>
          <Form.Group >
            <Form.Label >Last Name</Form.Label>
            <Form.Control
              name="lastName"
              type="text"
              placeholder="Last Name"
              value={props.values.lastName}
              onChange={props.handleChange}
              isInvalid={!!props.errors.lastName}
            />
            <Form.Control.Feedback type="invalid">{props.errors.lastName}</Form.Control.Feedback>
          </Form.Group>
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
            <Form.Label >Password</Form.Label>
            <Form.Control
              name="password1"
              type="password"
              placeholder="Password"
              value={props.values.password1}
              onChange={props.handleChange}
              isInvalid={!!props.errors.password1}
            />
            <Form.Control.Feedback type="invalid">{props.errors.password1}</Form.Control.Feedback>
          </Form.Group>
          <Form.Group >
            <Form.Label >Confirm Password</Form.Label>
            <Form.Control
              name="password2"
              type="password"
              placeholder="Confirm Password"
              value={props.values.password2}
              onChange={props.handleChange}
              isInvalid={!!props.errors.password2}
            />
            <Form.Control.Feedback type="invalid">{props.errors.password2}</Form.Control.Feedback>
          </Form.Group>
          <Form.Check>
            <Form.Check.Input
              name="terms"
              onChange={props.handleChange}
              isInvalid={!!props.errors.terms}
            />
            <Form.Check.Label> Agree to <a href="/terms_of_service">terms of service</a></Form.Check.Label>
            <Form.Control.Feedback type="invalid">{props.errors.terms}</Form.Control.Feedback>
          </Form.Check>
          <Button type="submit">Submit form</Button>
          <br />
          <Form.Text className="text-danger">{props.status.failureMessage}</Form.Text>
          <Form.Text className="text-success">{props.status.successMessage}</Form.Text>
        </Form>
      )}
    </Formik>
  );
}


function Register() {
  return (
    <SimpleLayout>
      <div className="h-100 w-100 d-flex">
        <Card className="mx-auto my-auto">
          <Card.Body>
            <Card.Title>Register with <SchoolName /></Card.Title>
            <RegisterForm />
          </Card.Body>
        </Card>
      </div>
    </SimpleLayout>
  )
}

export default Register;
