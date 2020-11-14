import React from "react"
import SearchUserDropdown from "../components/SearchUserDropdown";
import { Formik, FormikHelpers } from "formik";
import { Row, Col, Modal, Button, Form } from "react-bootstrap";
import { newApptRequest, isApiErrorCode } from "../utils/utils";
import format from "date-fns/format";

type CreateApptRequestModalProps = {
  show: boolean;
  start: number;
  duration: number;
  setShow: (show: boolean) => void;
  apiKey: ApiKey;
}

function CreateApptRequestModal(props: CreateApptRequestModalProps) {

  type CreateApptRequestValue = {
    message: string,
    userId: number | null,
  }

  const onSubmit = async (values: CreateApptRequestValue,
    { setStatus, setErrors }: FormikHelpers<CreateApptRequestValue>) => {
    const maybeApptRequest = await newApptRequest({
      targetId: values.userId!,
      attending: true,
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
            userId: "This teacher does not exist."
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

    // On success close window
    props.setShow(false);
  }


  return <Modal
    className="CreateApptRequestModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Request Appointment with Teacher</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Formik<CreateApptRequestValue>
        onSubmit={onSubmit}
        initialValues={{
          message: "",
          userId: null
        }}
        initialStatus=""
      >
        {(fprops) => (
          <Form
            noValidate
            onSubmit={fprops.handleSubmit} >
            <Form.Group as={Row} controlId="startTime">
              <Form.Label column sm={2}>Start Time</Form.Label>
              <Col>
                <span>{format(props.start, "MMM do, hh:mm a")} </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="endTime">
              <Form.Label column sm={2}>End Time</Form.Label>
              <Col>
                <span>{format(props.start + props.duration, "MMM do, hh:mm a")} </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="userId">
              <Form.Label column sm={2}>Teacher ID</Form.Label>
              <Col>
                <SearchUserDropdown
                  invalid={!!fprops.errors.userId}
                  apiKey={props.apiKey}
                  userKind={"USER"} setFn={e => fprops.setFieldValue("userId", e)} />
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
            <Form.Control.Feedback type="invalid">{fprops.status}</Form.Control.Feedback>
          </Form>
        )}
      </Formik>
    </Modal.Body>
  </Modal>
}

export default CreateApptRequestModal;
