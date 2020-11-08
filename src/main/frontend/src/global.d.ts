declare global {

  type ApiErrorCode =
    "OK" |
    "NO_CAPABILITY" |
    "APIKEY_UNAUTHORIZED" |
    "DATABASE_INITIALIZED" |
    "PASSWORD_INCORRECT" |
    "PASSWORD_INSECURE" |
    "USER_NONEXISTENT" |
    "APIKEY_NONEXISTENT" |
    "USER_EXISTENT" |
    "APPT_REQUEST_NONEXISTENT" |
    "USER_NAME_EMPTY" |
    "USER_EMAIL_EMPTY" |
    "USER_EMAIL_INVALIDATED" |
    "USERKIND_INVALID" |
    "APPT_EXISTENT" |
    "ATTENDANCEKIND_INVALID" |
    "ATTENDANCE_EXISTENT" |
    "NEGATIVE_DURATION" |
    "VERIFICATIONKEY_NONEXISTENT" |
    "VERIFICATIONKEY_INVALID" |
    "VERIFICATIONKEY_TIMED_OUT" |
    "RESETKEY_NONEXISTENT" |
    "RESETKEY_INVALID" |
    "RESETKEY_TIMED_OUT" |
    "EMAIL_RATELIMIT" |
    "EMAIL_BLACKLISTED" |
    "UNKNOWN"  |
    "NETWORK";

  type SchoolInfo = {
    name: string,
    domain: string,
  }

  type UserKind = "STUDENT" | "USER" | "ADMIN"

  type EmailVerificationChallenge = {
    id: number,
    name: string,
    email: string,
    creationTime: number,
    kind: UserKind,
  }

  type ForgotPassword = {
    id: number,
    email: string,
    creationTime: number,
    valid: boolean,
  }

  type User = {
    id: number,
    kind: UserKind,
    name: string,
    email: string,
    validated: boolean,
  }

  type ApiKey = {
    id: number,
    creationTime: number,
    duration: number,
    key: string,
    creator: User,
    attendee: User,
    host: User,
  }

  type ApptRequest = {
    apptRequestId: number,
    creator: User
    attendee: User
    host: User
    message: string,
    creationTime: number,
    startTime: number
    duration: number
  }

  type Appt = {
    apptRequest: ApptRequest,
    message: string,
    creationTime: number,
    startTime: number,
    duration: number
  }

  type AttendanceKind = "PRESENT" | "TARDY" | "ABSENT"

  type Attendance = {
    appt: Appt,
    creationTime: number,
    kind: AttendanceKind,
  }

  interface AuthenticatedComponentProps {
    apiKey: ApiKey
    setApiKey: (data: ApiKey | null) => void
  }

  interface StudentComponentProps {
    apiKey: ApiKey
    setApiKey: (data: ApiKey | null) => void
  }
}
export { }
