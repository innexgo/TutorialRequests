import React from 'react'

import { Row, Col, Modal, Button, Form } from 'react-bootstrap';
import ToggleButton from "react-bootstrap/ToggleButton";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import { newAttendance, isApiErrorCode } from '../utils/utils';
import { Formik, FormikHelpers } from 'formik';
import format from 'date-fns/format';

type ApptTakeAttendanceModalProps = {
  show: boolean;
  setShow: (show: boolean) => void;
  appt: Appt;
  apiKey: ApiKey;
}

function ApptTakeAttendanceModal(props: ApptTakeAttendanceModalProps) {

  type TakeAttendanceValues = {
    attendanceKind: AttendanceKind
  }

  async function onSubmit(values: TakeAttendanceValues, { setErrors, setStatus }: FormikHelpers<TakeAttendanceValues>) {
    const maybeAttendance = await newAttendance({
      apptId: props.appt.apptRequest.apptRequestId,
      attendanceKind: values.attendanceKind,
      apiKey: props.apiKey.key,
    });

    if (isApiErrorCode(maybeAttendance)) {
      switch (maybeAttendance) {
        case "ATTENDANCE_EXISTENT": {
          setStatus("Attendance has already been taken");
          break;
        }
        case "APIKEY_NONEXISTENT": {
          setStatus("You have been automatically logged out. Please relogin.");
          break;
        }
        case "APIKEY_UNAUTHORIZED": {
          setStatus("You are not currently authorized to perform this action.");
          break;
        }
        default: {
          setStatus("An unknown or network error has occurred.");
          break;
        }
      }
    } else {
      props.setShow(false);
    }
  }

  return <Modal
    className="ApptTakeAttendanceModal"
    show={props.show}
    onHide={() => props.setShow(false)}
    keyboard={false}
    size="lg"
    centered
  >
    <Modal.Header closeButton>
      <Modal.Title id="modal-title">Take Attendance</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <Formik<TakeAttendanceValues>
        onSubmit={onSubmit}
        initialValues={{
          attendanceKind: "PRESENT"
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
                <span>{format(props.appt.startTime, "MMM do, hh:mm a")} </span>
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="endTime">
              <Form.Label column sm={2}>End Time</Form.Label>
              <Col>
                <span>{format(props.appt.startTime + props.appt.duration, "MMM do, hh:mm a")}</span>
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="student">
              <Form.Label column sm={2}>Student</Form.Label>
              <Col>
                <span>{props.appt.apptRequest.attendee.name}</span>
              </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="attendance">
              <Form.Label column sm={2}>Take Attendance</Form.Label>
              <Col>
                <div>
                  <ToggleButton
                    key={0}
                    type="radio"
                    name="radio"
                    value="PRESENT"
                    checked={fprops.values.attendanceKind === "PRESENT"}
                    onChange={_ => fprops.setFieldValue("attendanceKind", "PRESENT")}
                    className="btn-success"
                  >
                    Present
                  </ToggleButton>
                  <ToggleButton
                    key={1}
                    type="radio"
                    name="radio"
                    value="TARDY"
                    checked={fprops.values.attendanceKind === "TARDY"}
                    onChange={_ => fprops.setFieldValue("attendanceKind", "TARDY")}
                    className="btn-warning"
                  >
                    Tardy
                  </ToggleButton>
                  <ToggleButton
                    key={2}
                    type="radio"
                    name="radio"
                    value="ABSENT"
                    checked={fprops.values.attendanceKind === "ABSENT"}
                    onChange={_ => fprops.setFieldValue("attendanceKind", "ABSENT")}
                    className="btn-danger"
                  >
                    Absent
                  </ToggleButton>
                </div>
              </Col>
            </Form.Group>
            <br />
            <Button type="submit"> Submit </Button>
            <br />
            <Form.Text className="text-danger">{fprops.status}</Form.Text>
          </Form>
        )}
      </Formik>
    </Modal.Body>
  </Modal>
}

export default ApptTakeAttendanceModal;
