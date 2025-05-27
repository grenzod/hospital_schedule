import {
    IsString,
} from 'class-validator';

export class DoctorDTO {
    @IsString()
    license_number: string;

    experience_years: number;

    @IsString()
    description: string;

    constructor(data: any) {
        this.license_number = data.license_number;
        this.experience_years = data.experience_years;
        this.description = data.description;
    }
}