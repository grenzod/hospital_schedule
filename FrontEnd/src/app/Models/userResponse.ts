import { Gender } from "../Enums/gender.enum"
import { Role } from "./role"

export interface UserResponse {
    id: number
    address: string
    active: boolean
    full_name: string
    date_of_birth: Date
    phone_number: string
    email: string
    gender: Gender
    role: Role,
    avatar_url: string
    created_at: Date
}