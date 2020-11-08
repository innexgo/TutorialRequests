export function isApiErrorCode(maybeApiErrorCode: any): maybeApiErrorCode is ApiErrorCode {
  return typeof maybeApiErrorCode === 'string' &&
    maybeApiErrorCode in [
      "OK",
      "NO_CAPABILITY",
      "APIKEY_UNAUTHORIZED",
      "DATABASE_INITIALIZED",
      "PASSWORD_INCORRECT",
      "PASSWORD_INSECURE",
      "USER_NONEXISTENT",
      "APIKEY_NONEXISTENT",
      "USER_EXISTENT",
      "APPT_REQUEST_NONEXISTENT",
      "USER_NAME_EMPTY",
      "USER_EMAIL_EMPTY",
      "USER_EMAIL_INVALIDATED",
      "USERKIND_INVALID",
      "ATTENDANCEKIND_INVALID",
      "ATTENDANCE_EXISTENT",
      "NEGATIVE_DURATION",
      "VERIFICATION_KEY_NONEXISTENT",
      "VERIFICATION_KEY_INVALID",
      "VERIFICATION_KEY_TIMED_OUT",
      "ACCESS_KEY_NONEXISTENT",
      "ACCESS_KEY_INVALID",
      "ACCESS_KEY_TIMED_OUT",
      "EMAIL_RATELIMIT",
      "EMAIL_BLACKLISTED",
      "UNKNOWN",
      "NETWORK"
    ];

}

/**
 * Returns a promise that will be resolved in some milliseconds
 * use await sleep(some milliseconds)
 * @param {int} ms milliseconds to sleep for
 * @return {Promise} a promise that will resolve in ms milliseconds
 */
export function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function staticUrl() {
  return window.location.protocol + "//" + window.location.host;
}

export function apiUrl() {
  return staticUrl() + '/api';
}

// This function is guaranteed to only return ApiErrorCode | object
export async function fetchApi(url: string, data: any) {
  // Catch all errors and always return a response
  const resp = await (async () => {
    try {
      return await fetch(`${apiUrl()}/${url}`, {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      });
    } catch (e) {
      return new Response('"NETWORK"', { status: 400 })
    }
  })();

  try {
    let objResp = await resp.json();
    return objResp;
  } catch (e) {
    return "UNKNOWN";
  }
}

type newApiKeyProps = {
  userEmail: string,
  userPassword: string,
  duration: number
};

export async function newApiKey(props: newApiKeyProps): Promise<ApiKey | ApiErrorCode> {
  return await fetchApi("apiKey/new/", props);
}

type newEmailVerificationChallengeProps = {
  userName: string,
  userEmail: string,
  userKind: UserKind,
  userPassword: string,
};

export async function newEmailVerificationChallenge(props: newEmailVerificationChallengeProps): Promise<EmailVerificationChallenge | ApiErrorCode> {
  return await fetchApi("emailVerificationChallenge/new/", props);
}

type newUserProps = {
  verificationKey: string,
};

export async function newUser(props: newUserProps): Promise<User | ApiErrorCode> {
  return await fetchApi("user/new/", props);
}

type newForgotPasswordProps = {
  verificationKey: string,
};

export async function newForgotPassword(props: newForgotPasswordProps): Promise<ForgotPassword | ApiErrorCode> {
  return await fetchApi("forgotPassword/new/", props);
}

type newApptRequestProps = {
  targetId: number,
  attending: boolean,
  message: string,
  startTime: number,
  duration: number,
  apiKey: string,
};

export async function newApptRequest(props: newApptRequestProps): Promise<ApptRequest | ApiErrorCode> {
  return await fetchApi("apptRequest/new/", props);
}

type newApptProps = {
  apptRequestId: number,
  message: string,
  startTime: number,
  duration: number,
  apiKey: string,
};

export async function newAppt(props: newApptProps): Promise<Appt | ApiErrorCode> {
  return await fetchApi("appt/new/", props);
}

type newAttendanceProps = {
  apptId: number,
  attendanceKind: AttendanceKind,
  apiKey: string,
};

export async function newAttendance(props: newAttendanceProps): Promise<Attendance | ApiErrorCode> {
  return await fetchApi("attendance/new/", props);
}




type viewApptRequestsProps = {
  apptRequestId?: number,
  creatorId?: number,
  attendeeId?: number,
  hostId?: number,
  message?: string,
  creationTime?: number,
  minCreationTime?: number,
  maxCreationTime?: number,
  startTime?: number,
  minStartTime?: number,
  maxStartTime?: number,
  duration?: number,
  minDuration?: number,
  maxDuration?: number,
  confirmed?: boolean,
  offset?: number,
  count?: number,
  apiKey?: string
};

export async function viewApptRequests(props: viewApptRequestsProps): Promise<ApptRequest[] | ApiErrorCode> {
  return await fetchApi("apptRequest/", props);
}


type viewUserProps = {
  userId: number,
  userKind: UserKind,
  userName: string,
  partialUserName: string,
  userEmail: string,
  offset: number,
  count: number,
  apiKey: string
}

export async function viewUser(props: viewUserProps): Promise<User[] | ApiErrorCode> {
  return await fetchApi("user/", props);
}


