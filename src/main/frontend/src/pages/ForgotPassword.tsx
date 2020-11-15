import React from 'react';
import { Formik, FormikHelpers } from 'formik'
import { Button, Card, Form, } from 'react-bootstrap'
import { newForgotPassword, isApiErrorCode } from '../utils/utils';

import SimpleLayout from '../components/SimpleLayout';

function ForgotPasswordForm() {

  type ForgotPasswordValue = {
    email: string,
  }

  const onSubmit = async (values: ForgotPasswordValue, { setErrors, setStatus }: FormikHelpers<ForgotPasswordValue>) => {
    // Validate input
    if (values.email === "") {
      setErrors({ email: "Please enter your email" });
      return;
    }

    // Now send request
    const maybeForgotPassword = await newForgotPassword({
      userEmail: values.email
    });

    if (isApiErrorCode(maybeForgotPassword)) {
      switch (maybeForgotPassword) {
        case "USER_NONEXISTENT": {
          setErrors({ email: "No such user exists." });
          break;
        }
        case "EMAIL_RATELIMIT": {
          setErrors({ email: "Please wait 5 minutes before sending another email." });
          break;
        }
        case "EMAIL_BLACKLISTED": {
          setErrors({ email: "This email address has been blacklisted." });
          break;
        }
        default: {
          setStatus({
            failureMessage: "An unknown or network error has occured while trying to reset the password.",
            successMessage: ""
          });
          break;
        }
      }
      return;
    } else {
      setStatus({
        failureMessage: "",
        successMessage: "A reset email has been sent."
      });
    }
  }

  return (
    <Formik
      onSubmit={onSubmit}
      initialValues={{
        email: "",
      }}
      initialStatus={{
        failureMessage: "",
        successMessage: ""
      }}
    >
      {(props) => (
        <Form
          noValidate
          onSubmit={props.handleSubmit} >
          <Form.Group >
            <Form.Label column md={2}>Email</Form.Label>
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
          <br />
          <Button type="submit">Submit</Button>
          <br />
          <Form.Text className="text-danger">{props.status.failureMessage}</Form.Text>
          <Form.Text className="text-success">{props.status.successMessage}</Form.Text>
        </Form>
      )}
    </Formik>
  )
}

function ForgotPassword() {
  return <SimpleLayout>
    <div className="h-100 w-100 d-flex">
      <Card className="mx-auto my-auto">
        <Card.Body>
          <Card.Title>Send Reset Password Email</Card.Title>
          <ForgotPasswordForm />
        </Card.Body>
      </Card>
    </div>
  </SimpleLayout>
}

export default ForgotPassword;
