import React from 'react';
import { OverlayTrigger, Card } from 'react-bootstrap';
import { Help } from '@material-ui/icons';
import ErrorBoundary from '../components/ErrorBoundary';
import Loader from '../components/Loader';
import { Async, FulfilledChildren } from 'react-async';


interface UtilityProps<DataType> {
  title: string
  overlay: React.ReactElement
  promise: Promise<DataType>
  handler: (error: Error) => React.ReactElement,
  children:FulfilledChildren<DataType>
}

// function is generic over dataType
function Utility<DataType>(props: React.PropsWithChildren<UtilityProps<DataType>>) {
  return <Card>
    <Card.Body>
      <div className="d-flex justify-content-between">
        <Card.Title >{props.title}</Card.Title>
        <OverlayTrigger
          overlay={props.overlay}
          placement="auto"
        >
          <button type="button" className="btn btn-sm">
            <Help />
          </button>
        </OverlayTrigger>
      </div>
      <Async promise={props.promise}>
        <Async.Pending><Loader /></Async.Pending>
        <Async.Rejected>handler</Async.Rejected>
        <Async.Fulfilled<DataType>>
          <ErrorBoundary handler={props.handler}>
            {props.children}
          </ErrorBoundary>
        </Async.Fulfilled>
      </Async>
    </Card.Body>
  </Card>
}

export default Utility;
