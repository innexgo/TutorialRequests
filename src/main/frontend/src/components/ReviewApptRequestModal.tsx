import React from "react";
import FullCalendar, { EventChangeArg, DateSelectArg } from "@fullcalendar/react"
import interactionPlugin from '@fullcalendar/interaction'
import timeGridPlugin from '@fullcalendar/timegrid'
import { Row, Col, Modal, Button, Form } from 'react-bootstrap';
import { Formik, FormikHelpers } from 'formik';
import format from 'date-fns/format';

import { newAppt, isApiErrorCode } from '../utils/utils';

type ReviewApptRequestModalProps = {
  show: boolean;
  setShow: (show: boolean) => void;
  apptRequest: ApptRequest;
  apiKey: ApiKey;
}

function ReviewApptRequestModal(props: ReviewApptRequestModalProps) {
  type ReviewApptRequestValues = {
    message: string,
  }

  const [duration, setDuration] = React.useState(props.apptRequest.duration);
  const [startTime, setStartTime] = React.useState(props.apptRequest.startTime);

  const onSubmit = async (values: ReviewApptRequestValues,
    { setStatus }: FormikHelpers<ReviewApptRequestValues>) => {

    const maybeAppt = await newAppt({
      apptRequestId: props.apptRequest.apptRequestId,
      message: values.message,
      startTime: startTime,
      duration: duration,
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

  const eventChangeHandler = (eca: EventChangeArg) => {
    const start = eca.event.start!.valueOf();
    const end = eca.event.end!.valueOf();
    setStartTime(start);
    setDuration(end - start);
  }

  return <Modal
    className="ReviewApptRequestModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title>Review Student Request</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Row>
        <Col>
          <Formik<ReviewApptRequestValues>
            onSubmit={onSubmit}
            initialValues={{
              message: ""
            }}
            initialStatus=""
          >
            {(fprops) => (
              <Form>
                <Form.Group as={Row}>
                  <Form.Label column sm={4}>Start Time</Form.Label>
                  <Col>
                    <span >
                      {format(startTime, "MMM do, hh:mm a")}
                    </span>
                  </Col>
                </Form.Group>
                <Form.Group as={Row}>
                  <Form.Label column sm={4}>End Time</Form.Label>
                  <Col>
                    <span>
                      {format(startTime + duration, "MMM do, hh:mm a")}
                    </span>
                  </Col>
                </Form.Group>
                <Form.Group as={Row}>
                  <Form.Label column sm={4}>Student</Form.Label>
                  <Col>
                    <span  >
                      {props.apptRequest.attendee.name}
                    </span>
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
                <Button type="submit" > Accept </Button>
                <Button variant="danger" onClick={() => props.setShow(false)}> Ignore </Button>
                <br />
                <Form.Control.Feedback type="invalid">{fprops.status}</Form.Control.Feedback>
              </Form>)}
          </Formik>
        </Col>
        <Col>
          <FullCalendar
            plugins={[timeGridPlugin, interactionPlugin]}
            initialView="timeGridDay"
            unselectCancel=".ReviewApptRequestModal"
            headerToolbar={false}
            allDaySlot={false}
            slotMinTime="08:00"
            slotMaxTime="18:00"
            eventChange={eventChangeHandler}
            selectable={true}
            selectMirror={true}
            initialDate={props.apptRequest.startTime}
            height="auto"
            events={[{
              start: props.apptRequest.startTime,
              end: props.apptRequest.startTime + props.apptRequest.duration,
              display: "background"
            }]}
            select={(dsa: DateSelectArg) => {
              setStartTime(dsa.start.valueOf());
              setDuration(dsa.end.valueOf() - dsa.start.valueOf());
            }}
            unselect={() => {
              setStartTime(props.apptRequest.startTime);
              setDuration(props.apptRequest.duration);
            }}
          />
        </Col>
      </Row>
    </Modal.Body>
  </Modal>
}

export default ReviewApptRequestModal;
