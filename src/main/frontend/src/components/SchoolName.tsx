import React from 'react'
import { fetchApi } from '../utils/utils'
import { Async } from 'react-async';

const SchoolName = () =>
  <Async promise={fetchApi('')}>
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

export default SchoolName
