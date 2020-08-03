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
