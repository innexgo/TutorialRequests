import React from 'react'
import { schoolInfo, isApiErrorCode } from '../utils/utils'
import { Async } from 'react-async';

const SchoolName = () => {
  const getSchoolInfo = async () => {
      const maybeSchoolInfo = await schoolInfo();
      if(isApiErrorCode(maybeSchoolInfo)) {
          throw new Error();
      } else {
          return maybeSchoolInfo;
      }
  }
  return <Async promise={getSchoolInfo()}>
    <Async.Pending>
      Innexgo Hours
    </Async.Pending>
    <Async.Rejected>
      Innexgo Hours
    </Async.Rejected>
    <Async.Fulfilled<SchoolInfo>>
      {schoolInfo => `Innexgo Hours: ${schoolInfo.name}`}
    </Async.Fulfilled>
  </Async>
}

export default SchoolName
