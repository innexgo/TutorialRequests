declare global {
  type SchoolInfo = {
    id: number,
    name: string,
    domain: string,
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
    creator: User,
  }

  type ApptRequest = {
    apptRequestId: number,
    creator: User
    target: User
    message: string,
    creationTime: number,
    suggestedTime: number
  }

  type Appt = {
    apptRequest: ApptRequest,
    message: string,
    creationTime: number,
    startTime: number,
    duration: number
  }

  type Attendance = {
    appt: Appt,
    creationTime: number,
    kind: "PRESENT" | "TARDY" | "ABSENT",
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
