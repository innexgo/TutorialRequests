declare module '*.png'

type School = {
  id: number,
  name: string
}

enum UserKind {
  Student,
  User,
  Admin,
}

type User = {
  id: number,
  secondaryId: number,
  school: School,
  kind: UserKind,
  name: string,
  email: string,
}

type ApiKey = {
  id: number,
  creationTime: number,
  duration: number,
  canLogIn: boolean,
  canChangePassword: boolean,
  canReadUser: boolean,
  canWriteUser: boolean,
  canReadApptRequest: boolean,
  canWriteApptRequest: boolean,
  canReadAppt: boolean,
  canWriteAppt: boolean,
  canReadAttendance: boolean,
  canWriteAttendance: boolean,
  key: string,
  user: User,
}

type ApptRequest = {
  id:number,
  creator:Student
  target:User
  message:string,
  creationTime:number,
  suggestedTime:boolean
}

type Appt = {
  id: number,
  host:User,
  attendee:User,
  apptRequest:ApptRequest,
  message:string,
  creationTime: number,
  startTime:number,
  duration:number
}

type Attendance = {
  id: number,
  appt:Appt,
  creationTime: number
}

interface AuthenticatedComponentProps {
  apiKey: ApiKey
  setApiKey: (data: ApiKey | null) => void
}
