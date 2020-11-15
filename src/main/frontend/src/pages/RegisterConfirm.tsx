import React from 'react';
import { Card, } from 'react-bootstrap'
import { Async } from 'react-async';
import SimpleLayout from '../components/SimpleLayout';
import Loader from '../components/Loader';
import { newUser, isApiErrorCode } from '../utils/utils';

function RegisterConfirmError(prop: { maybeUser: any }) {
  if (isApiErrorCode(prop.maybeUser)) {
    switch (prop.maybeUser) {
      case "VERIFICATIONKEY_NONEXISTENT": {
        return <Card.Text>
          Verification link is invalid.
          Click <a href="/register">here</a> to register.
        </Card.Text>;
      }
      case "VERIFICATIONKEY_TIMED_OUT": {
        return <Card.Text>
          Verification link has timed out.
          Click <a href="/register">here</a> to register again.
        </Card.Text>;
      }
      case "USER_EXISTENT": {
        return <Card.Text>
          A user with these credentials already exists.
        </Card.Text>;
      }
      default: {
        return <Card.Text>
          An unknown or network error has occured.
        </Card.Text>;
      }
    }
  } else {
    return <Card.Text>
      Your account ({prop.maybeUser.email}) has been sucessfully created.
      Click <a href="/">here</a> to login.
    </Card.Text>
  }
}

function RegisterConfirm() {
  const maybeUserPromise = newUser({
    verificationKey: new URLSearchParams(window.location.search).get("verificationKey") ?? ""
  });

  return (
    <SimpleLayout>
      <div className="h-100 w-100 d-flex">
        <Card className="mx-auto my-auto">
          <Card.Body>
            <Card.Title>Complete Account Registration</Card.Title>
            <Async promise={maybeUserPromise}>
              <Async.Pending>
                <div>
                  <br />
                  <Loader />
                  <br />
                </div>
              </Async.Pending>
              <Async.Resolved>
                {(maybeUser) => <RegisterConfirmError maybeUser={maybeUser} />}
              </Async.Resolved>
            </Async>
          </Card.Body>
        </Card>
      </div>
    </SimpleLayout>
  )
}

export default RegisterConfirm;
