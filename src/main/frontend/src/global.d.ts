declare global {
  type SchoolInfo = {
    name: string,
    domain: string,
  }

  type UserKind = "STUDENT" | "USER" | "ADMIN"

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
export {}
