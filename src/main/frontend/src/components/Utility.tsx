import React from 'react';
import { OverlayTrigger, Card } from 'react-bootstrap';
import { Help } from '@material-ui/icons';
import ErrorBoundary from '../components/ErrorBoundary';
import Loader from '../components/Loader';
import { Async, AsyncFulfilled } from 'react-async';


interface UtilityProps<T> {
  title: string
  promise: Promise<T>
  children: [React.ReactElement, ((data: T, state: AsyncFulfilled<T>) => React.ReactNode)]
}

// function is generic over dataType
function Utility<DataType>(props: UtilityProps<DataType>) {
  const handler = (error: Error) => <h1>Something went wrong: {error.message}</h1>;
  return <Card>
    <Card.Body>
      <div className="d-flex justify-content-between">
        <Card.Title >{props.title}</Card.Title>
        <OverlayTrigger
          overlay={props.children[0]}
          placement="auto"
        >
          <button type="button" className="btn btn-sm">
            <Help />
          </button>
        </OverlayTrigger>
      </div>
      <ErrorBoundary handler={handler}>
        <Async promise={props.promise}>
          <Async.Pending><Loader /></Async.Pending>
          <Async.Rejected> {handler} </Async.Rejected>
          <Async.Fulfilled<DataType>>
            {props.children[1]}
          </Async.Fulfilled>
        </Async>
      </ErrorBoundary>
    </Card.Body>
  </Card>
}

export default Utility;
