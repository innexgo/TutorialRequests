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

function getFormData(data: object) {
  const formData = new FormData();
  Object.keys(data).forEach(key => formData.append(key, (data as any)[key]));
  return formData;
}

// This function is guaranteed to only return ApiErrorCode | object
export async function fetchApi(url: string, data: FormData) {
  // Catch all errors and always return a response
  const resp = await (async () => {
    try {
      return await fetch(`${apiUrl()}/${url}`, {
        method: 'POST',
        body: data
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

const ApiErrorCodes = [
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
  "APPT_EXISTENT",
  "ATTENDANCEKIND_INVALID",
  "ATTENDANCE_EXISTENT",
  "NEGATIVE_DURATION",
  "VERIFICATIONKEY_NONEXISTENT",
  "VERIFICATIONKEY_TIMED_OUT",
  "RESETKEY_NONEXISTENT",
  "RESETKEY_INVALID",
  "RESETKEY_TIMED_OUT",
  "EMAIL_RATELIMIT",
  "EMAIL_BLACKLISTED",
  "UNKNOWN",
  "NOT_FOUND",
  "NETWORK"
] as const;

// Creates a union type
export type ApiErrorCode = typeof ApiErrorCodes[number];

type NewApiKeyProps = {
  userEmail: string,
  userPassword: string,
  duration: number
};

export async function newApiKey(props: NewApiKeyProps): Promise<ApiKey | ApiErrorCode> {
  return await fetchApi("apiKey/new/", getFormData(props));
}

type NewEmailVerificationChallengeProps = {
  userName: string,
  userEmail: string,
  userKind: UserKind,
  userPassword: string,
};

export async function newEmailVerificationChallenge(props: NewEmailVerificationChallengeProps): Promise<EmailVerificationChallenge | ApiErrorCode> {
  return await fetchApi("emailVerificationChallenge/new/", getFormData(props));
}

type NewUserProps = {
  verificationKey: string,
};

export async function newUser(props: NewUserProps): Promise<User | ApiErrorCode> {
  return await fetchApi("user/new/", getFormData(props));
}

type NewForgotPasswordProps = {
  userEmail: string,
};

export async function newForgotPassword(props: NewForgotPasswordProps): Promise<ForgotPassword | ApiErrorCode> {
  return await fetchApi("forgotPassword/new/", getFormData(props));
}

type NewApptRequestProps = {
  targetId: number,
  attending: boolean,
  message: string,
  startTime: number,
  duration: number,
  apiKey: string,
};

export async function newApptRequest(props: NewApptRequestProps): Promise<ApptRequest | ApiErrorCode> {
  return await fetchApi("apptRequest/new/", getFormData(props));
}

type NewApptProps = {
  apptRequestId: number,
  message: string,
  startTime: number,
  duration: number,
  apiKey: string,
};

export async function newAppt(props: NewApptProps): Promise<Appt | ApiErrorCode> {
  return await fetchApi("appt/new/", getFormData(props));
}

type NewAttendanceProps = {
  apptId: number,
  attendanceKind: AttendanceKind,
  apiKey: string,
};

export async function newAttendance(props: NewAttendanceProps): Promise<Attendance | ApiErrorCode> {
  return await fetchApi("attendance/new/", getFormData(props));
}

type ViewUserProps = {
  userId?: number,
  userKind?: UserKind,
  userName?: string,
  partialUserName?: string,
  userEmail?: string,
  offset?: number,
  count?: number,
  apiKey: string
}

export async function viewUser(props: ViewUserProps): Promise<User[] | ApiErrorCode> {
  return await fetchApi("user/", getFormData(props));
}

type ViewApptRequestProps = {
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
  apiKey: string
};

export async function viewApptRequest(props: ViewApptRequestProps): Promise<ApptRequest[] | ApiErrorCode> {
  return await fetchApi("apptRequest/", getFormData(props));
}


type ViewApptProps = {
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
  attended?: boolean,
  offset?: number,
  count?: number,
  apiKey: string
}


export async function viewAppt(props: ViewApptProps): Promise<Appt[] | ApiErrorCode> {
  return await fetchApi("appt/", getFormData(props));
}

type ViewAttendanceProps = {
  apptId?: number,
  attendeeId?: number,
  hostId?: number,
  creationTime?: number,
  minCreationTime?: number,
  maxCreationTime?: number,
  startTime?: number,
  minStartTime?: number,
  maxStartTime?: number,
  kind?: AttendanceKind,
  offset?: number,
  count?: number,
  apiKey: string
}

export async function viewAttendance(props: ViewAttendanceProps): Promise<Attendance[] | ApiErrorCode> {
  return await fetchApi("attendance/", getFormData(props));
}


type UpdatePasswordProps = {
  userId: number,
  oldPassword: string,
  newPassword: string,
  apiKey: string,
}

export async function updatePassword(props: UpdatePasswordProps): Promise<ApiErrorCode> {
  return await fetchApi("misc/updatePassword/", getFormData(props));
}

export async function schoolInfo(): Promise<SchoolInfo | ApiErrorCode> {
  return await fetchApi("misc/info/school/", getFormData({}));
}

type ResetPasswordProps = {
  resetKey: String,
  newPassword: String,
}

export async function resetPassword(props: ResetPasswordProps): Promise<ApiErrorCode> {
  return await fetchApi("misc/resetPassword/", getFormData(props));
}

export function isApiErrorCode(maybeApiErrorCode: any): maybeApiErrorCode is ApiErrorCode {
  return typeof maybeApiErrorCode === 'string' && ApiErrorCodes.includes(maybeApiErrorCode as any);
}

export const isPasswordValid = (pass: string) => pass.length >= 8 && /\d/.test(pass);
