import React from 'react';
import ErrorBoundary from '../components/ErrorBoundary';
import Loader from '../components/Loader';
import { Async, AsyncFulfilled } from 'react-async';
import UtilityWrapper from '../components/UtilityWrapper';


interface UtilityProps<T> {
  title: string
  promise: Promise<T>
  children: [React.ReactElement, ((data: T, state: AsyncFulfilled<T>) => React.ReactNode)]
}

// function is generic over dataType
function Utility<DataType>(props: UtilityProps<DataType>) {
  const handler = (error: Error) => <h1>Something went wrong: {error.message}</h1>;
  return <UtilityWrapper title={props.title}>
    {props.children[0]}
    <ErrorBoundary handler={handler}>
      <Async promise={props.promise}>
        <Async.Pending><Loader /></Async.Pending>
        <Async.Rejected> {handler} </Async.Rejected>
        <Async.Fulfilled<DataType>>
          {props.children[1]}
        </Async.Fulfilled>
      </Async>
    </ErrorBoundary>
  </UtilityWrapper>
}

export default Utility;
