declare module '*.png'

type Student = {
  id: number,
  name: string
}

type User = {
  id: number,
  name: string,
  email: string,
}

type ApptRequest = {
    id:number,
    student:Student
    user:User
    message:string,
    creationTime:number,
    requestTime:number,
    reviewed:boolean,
    approved:boolean,
    response:string,
    present:boolean
}


type ApiKey = {
  id: number,
  administrator: boolean,
  creationTime: number,
  expirationTime: number,
  key: string,
  user: User,
}

interface AuthenticatedComponentProps {
  apiKey: ApiKey
  setApiKey: (data: ApiKey | null) => void
}

interface StudentComponentProps {
  student: Student
  setStudent: (data: Student | null) => void
}
