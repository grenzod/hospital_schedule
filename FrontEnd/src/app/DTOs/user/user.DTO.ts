import {
    IsString,
    IsNotEmpty,
    IsEnum,
    IsDateString
} from 'class-validator';
import { Gender } from '../../Enums/gender.enum';

export class UserDTO {
    @IsString()
    @IsNotEmpty()
    phone_number!: string;

    @IsString()
    @IsNotEmpty()
    full_name!: string;

    @IsEnum(Gender)
    gender!: Gender;

    @IsString()
    @IsNotEmpty()
    address!: string;

    @IsDateString()
    date_of_birth!: string;

    constructor(data: any) {
        this.phone_number = data.phone_number;
        this.full_name = data.full_name;
        this.gender = data.gender;
        this.address = data.hometown;
        this.date_of_birth = data.birthDate;
    }
}