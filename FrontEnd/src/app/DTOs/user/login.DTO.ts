import {
    IsString,
    IsNotEmpty,
    IsEmail
} from 'class-validator'

export class LoginDTO {
    @IsEmail()
    email: string

    @IsString()
    @IsNotEmpty()
    password: string;

    constructor(data: any) {
        this.email = data.email;
        this.password = data.password;
    }
}