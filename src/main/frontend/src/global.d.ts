declare global {
  type SchoolInfo = {
    name: string
  }

  type User = {
    id: number,
    secondaryId: number,
    school: School,
    kind: "STUDENT" | "USER" | "ADMIN",
    name: string,
    email: string,
  }

  type ApiKey = {
    id: number,
    creationTime: number,
    duration: number,
    key: string,
    user: User,
  }

  type ApptRequest = {
    id: number,
    creator: User
    target: User
    message: string,
    creationTime: number,
    suggestedTime: number
  }

  type Appt = {
    id: number,
    host: User,
    attendee: User,
    apptRequest: ApptRequest,
    message: string,
    creationTime: number,
    startTime: number,
    duration: number
  }

  type Attendance = {
    id: number,
    appt: Appt,
    creationTime: number,
    kind: "PRESENT" | "TARDY" | "ABSENT",
  }

  interface AuthenticatedComponentProps {
    apiKey: ApiKey
    setApiKey: (data: ApiKey | null) => void
  }
}
export {}
