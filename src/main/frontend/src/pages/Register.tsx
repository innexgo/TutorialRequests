import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Button, Row, Col, Form, } from 'react-bootstrap'

import { fetchApi } from '../utils/utils';

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

  const onSubmit = (values: RegistrationValue, { setErrors }: FormikHelpers<RegistrationValue>) => {
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
    setErrors(errors);
    if (hasError) {
      return;
    }

    // Now send request
    try {
      const apiKey = await fetchApi(`verificationChallenge/new/?` + new URLSearchParams([
        ['userName', `${values.firstName.trim()} ${values.lastName.trim()}`],
        ['userEmail', values.email],
        ['userPassword', values.password1],
      ])) as ApiKey;
      props.setApiKey(apiKey);
    } catch (e) {
      console.log(e);
      setErrors({
          password:"Your username or password is incorrect."
      });
    }

  }

  return (
    <Formik
      onSubmit={onSubmit}
      initialValues={{
        firstName: "A",
        lastName: "B",
        email: "C",
        password1: "Boolean500",
        password2: "Boolean500",
        terms: true,
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
