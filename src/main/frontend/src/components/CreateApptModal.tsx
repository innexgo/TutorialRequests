import React from 'react'
import SearchUserDropdown from '../components/SearchUserDropdown';
import { Formik, FormikHelpers } from 'formik';

import { Row, Col, Modal, Button, Form } from 'react-bootstrap';
import { newApptRequest, newAppt, isApiErrorCode } from '../utils/utils';
import format from 'date-fns/format';

type CreateApptModalProps = {
  show: boolean;
  start: number;
  duration: number;
  setShow: (show: boolean) => void;
  apiKey: ApiKey;
}

function CreateApptModal(props: CreateApptModalProps) {

  type CreateApptValue = {
    message: string,
    studentId: number | null,
  }

  const onSubmit = async (values: CreateApptValue, { setStatus, setErrors }: FormikHelpers<CreateApptValue>) => {
    const maybeApptRequest = await newApptRequest({
      targetId: values.studentId!,
      attending: false,
      message: values.message,
      startTime: props.start,
      duration: props.duration,
      apiKey: props.apiKey.key,
    });

    if (isApiErrorCode(maybeApptRequest)) {
      switch (maybeApptRequest) {
        case "APIKEY_NONEXISTENT": {
          setStatus("You have been automatically logged out. Please relogin.");
          break;
        }
        case "USER_NONEXISTENT": {
          setErrors({
            studentId: "This student does not exist."
          });
          break;
        }
        case "NEGATIVE_DURATION": {
          setStatus("The duration you have selected is not valid.");
          break;
        }
        default: {
          setStatus("An unknown or network error has occurred.");
          break;
        }
      }
      return;
    }

    const maybeAppt = await newAppt({
      apptRequestId: maybeApptRequest.apptRequestId,
      message: values.message,
      startTime: props.start,
      duration: props.duration,
      apiKey: props.apiKey.key
    });

    if (isApiErrorCode(maybeAppt)) {
      switch (maybeAppt) {
        case "APIKEY_NONEXISTENT": {
          setStatus("You have been automatically logged out. Please relogin.");
          break;
        }
        case "APIKEY_UNAUTHORIZED": {
          setStatus("You are not currently authorized to perform this action.");
          break;
        }
        case "APPT_EXISTENT": {
          setStatus("This appointment already exists.");
          break;
        }
        default: {
          setStatus("An unknown or network error has occurred.");
          break;
        }
      }
    } else {
      // On success close window
      props.setShow(false);
    }
  }

  return <Modal
    className="CreateApptModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Create Appointment with Student</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Formik<CreateApptValue>
        onSubmit={onSubmit}
        initialValues={{
          message: "",
          studentId: null
        }}
        initialStatus=""
      >
        {(fprops) => (
          <Form
            noValidate
            onSubmit={fprops.handleSubmit} >
            <Form.Group as={Row}>
              <Form.Label column sm={2}>Start Time</Form.Label>
              <Col>
                <span>{format(props.start, "MMM do, hh:mm a")} </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row}>
              <Form.Label column sm={2}>End Time</Form.Label>
              <Col>
                <span>{format(props.start + props.duration, "MMM do, hh:mm a")} </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row}>
              <Form.Label column sm={2}>Student ID</Form.Label>
              <Col>
                <SearchUserDropdown
                  invalid={!!fprops.errors.studentId}
                  apiKey={props.apiKey}
                  userKind={"STUDENT"} setFn={e => fprops.setFieldValue("studentId", e)} />
              </Col>
            </Form.Group>
            <Form.Group as={Row}>
              <Form.Label column sm={2}>Message</Form.Label>
              <Col>
                <Form.Control
                  name="message"
                  type="text"
                  placeholder="Message"
                  as="textarea"
                  rows={3}
                  onChange={fprops.handleChange}
                  isInvalid={!!fprops.errors.message}
                />
                <Form.Control.Feedback type="invalid">{fprops.errors.message}</Form.Control.Feedback>
              </Col>
            </Form.Group>
            <Button type="submit"> Submit </Button>
            <br />
            <Form.Text className="text-danger">{fprops.status}</Form.Text>
          </Form>)}
      </Formik>
    </Modal.Body>
  </Modal>
}

export default CreateApptModal;
