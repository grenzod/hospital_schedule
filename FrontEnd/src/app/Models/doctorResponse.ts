export interface DoctorResponse {
    id: number
    full_name: string
    department_name: string
    license_number: string;
    experience_years: number;
    description: string;
    is_available: boolean;
    avatar_url: string;
}