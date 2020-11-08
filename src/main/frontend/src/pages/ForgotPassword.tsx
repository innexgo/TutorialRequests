import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Button, Row, Col, Form, } from 'react-bootstrap'
import { fetchApi } from '../utils/utils';

import SimpleLayout from '../components/SimpleLayout';

function ForgotPasswordForm() {

  type ForgotPasswordValue = {
    email: string,
  }

  const onSubmit = async (values: ForgotPasswordValue, { setErrors }: FormikHelpers<ForgotPasswordValue>) => {
    // Validate input
    let errors: FormikErrors<ForgotPasswordValue> = {};
    let hasError = false;
    if (values.email === "") {
      errors.email = "Please enter your email";
      hasError = true;
    }

    setErrors(errors);
    if (hasError) {
      return;
    }

    // Now send request
    try {
      const apiKey = await fetchApi(`forgotPassword/new/?` + new URLSearchParams([
        ['userEmail', values.email],
        ['duration', `${5 * 60 * 60 * 1000}`], // 5 hours
      ])) as ApiKey;
    } catch (e) {
      console.log(e);
      setErrors({
        email: "Your username or password is incorrect."
      });
    }

  }

  return (
    <Formik
      onSubmit={onSubmit}
      initialValues={{
        email: "",
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
          <Button type="submit">Submit</Button>
        </Form>
      )}
    </Formik>
  )
}

function ForgotPassword() {
  return (
    <SimpleLayout>
      <div className="px-3 py-3">
        <h4>Send Password Reset</h4>
        <ForgotPasswordForm />
      </div>
    </SimpleLayout>
  );
}

export default ForgotPassword;
