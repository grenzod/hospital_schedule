import { Injectable } from "@angular/core";
import { environment } from "../Environments/environment";
import { JwtHelperService } from "@auth0/angular-jwt";
import { CookieService } from "ngx-cookie-service";

@Injectable({
    providedIn: 'root'
})
export class TokenService {
    private readonly TOKEN_KEY = 'access_token';
    private readonly COOKIE_DOMAIN = environment.cookieDomain;
    private jwtHelper = new JwtHelperService();

    private constructor(private cookieService: CookieService) { }

    setToken(token: string, expiration: Date) {
        const expirationDays = (expiration.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24);

        this.cookieService.set(
            this.TOKEN_KEY,
            token,
            {
                expires: expirationDays,
                secure: true,
                sameSite: 'Strict',
                domain: this.COOKIE_DOMAIN,
                path: '/'
            }
        );
    }

    getToken(): string | null {
        try {
            const token = this.cookieService.get(this.TOKEN_KEY);
            if (!token) return null;
            if (this.jwtHelper.isTokenExpired(token)) {
                this.clearToken();
                return null;
            }
            return token;
        } catch {
            return null;
        }
    }

    clearToken() {
        this.cookieService.delete(
            this.TOKEN_KEY,
            '/',
            this.COOKIE_DOMAIN,
            true,  
            'Strict' 
        );
    }

    isLoggedIn(): boolean {
        return this.getToken() !== null;
    }

    getTokenExpirationDate(): Date | null {
        const token = this.getToken();
        if (!token) return null;
        return this.jwtHelper.getTokenExpirationDate(token);
    }
}