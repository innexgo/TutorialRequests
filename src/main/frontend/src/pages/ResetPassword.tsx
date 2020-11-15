import React from 'react';
import { Formik, FormikHelpers, FormikErrors } from 'formik'
import { Card, Button, Form, } from 'react-bootstrap'
import { resetPassword, isApiErrorCode, isPasswordValid } from '../utils/utils';

import SimpleLayout from '../components/SimpleLayout';

interface ResetPasswordProps {
  resetKey: string,
}

function ResetPasswordForm(props: ResetPasswordProps) {

  type ResetPasswordValue = {
    password1: string,
    password2: string,
  }

  const onSubmit = async (values: ResetPasswordValue, { setStatus, setErrors }: FormikHelpers<ResetPasswordValue>) => {
    // Validate input
    let errors: FormikErrors<ResetPasswordValue> = {};
    let hasError = false;
    if (!isPasswordValid(values.password1)) {
      errors.password1 = "Password must have at least 8 chars and 1 number";
      hasError = true;
    }
    if (values.password2 !== values.password1) {
      errors.password2 = "Password does not match.";
      hasError = true;
    }
    setErrors(errors);
    if (hasError) {
      return;
    }

    const maybeApiKey = await resetPassword({
      resetKey: props.resetKey,
      newPassword: values.password1,
    });

    if (!isApiErrorCode(maybeApiKey)) {
      // on success set the api key
      setStatus({
        failureMessage: "",
        successMessage: "Password successfully changed."
      });
    } else {
      // otherwise display errors
      switch (maybeApiKey) {
        case "RESETKEY_NONEXISTENT": {
          setStatus({
            failureMessage: "Invalid password reset link.",
            successMessage: ""
          });
          break;
        }
        case "RESETKEY_TIMED_OUT": {
          setStatus({
            failureMessage: "Password reset link timed out.",
            successMessage: ""
          });
          break;
        }
        case "RESETKEY_INVALID": {
          setStatus({
            failureMessage: "Password reset link may only be used once.",
            successMessage: ""
          });
          break;
        }
        case "PASSWORD_INSECURE": {
          setErrors({
            password1: "Password is of insufficient complexity"
          });
          break;
        }
        default: {
          console.log(maybeApiKey);
          setStatus({
            failureMessage: "An unknown or network error has occured while trying to reset password.",
            successMessage: ""
          });
          break;
        }
      }
      return;
    }
  }

  return <>
    <Formik<ResetPasswordValue>
      onSubmit={onSubmit}
      initialStatus={{
        successMessage: "",
        failureMessage: "",
      }}
      initialValues={{
        password1: "",
        password2: "",
      }}
    >
      {(props) => (
        <Form
          noValidate
          onSubmit={props.handleSubmit} >
          <Form.Group >
            <Form.Label >New Password</Form.Label>
            <Form.Control
              name="password1"
              type="password"
              placeholder="New Password"
              value={props.values.password1}
              onChange={props.handleChange}
              isInvalid={!!props.errors.password1}
            />
            <Form.Control.Feedback type="invalid"> {props.errors.password1} </Form.Control.Feedback>
          </Form.Group>
          <Form.Group >
            <Form.Label>Confirm Password</Form.Label>
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
          <br />
          <Button type="submit">Reset Password</Button>
          <br />
          <Form.Text className="text-danger">{props.status.failureMessage}</Form.Text>
          <Form.Text className="text-success">{props.status.successMessage}</Form.Text>
        </Form>
      )}
    </Formik>
  </>
}

function ResetPassword() {
  const resetKey = new URLSearchParams(window.location.search).get("resetKey") ?? "";
  return <SimpleLayout>
    <div className="h-100 w-100 d-flex">
      <Card className="mx-auto my-auto">
        <Card.Body>
          <Card.Title>Reset Password</Card.Title>
          <ResetPasswordForm resetKey={resetKey} />
        </Card.Body>
      </Card>
    </div>
  </SimpleLayout>
}

export default ResetPassword;
