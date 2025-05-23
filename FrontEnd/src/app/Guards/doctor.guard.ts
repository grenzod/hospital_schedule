import { inject, Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from "@angular/router";
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { TokenService } from "../Services/token.service";
import { UserService } from "../Services/user.service";

@Injectable({
    providedIn: 'root'
})
export class DoctorGuard {
    constructor(
        private tokenService: TokenService, 
        private router: Router,
        private userService: UserService
    ) { }

    canActivate(): Observable<boolean> {
        const token = this.tokenService.getToken();
        if (!token) {
            this.handleAuthFailure();
            return of(false);
        }

        const expirationDate = this.tokenService.getTokenExpirationDate();
        if (expirationDate && expirationDate < new Date()) {
            this.handleAuthFailure();
            return of(false);
        }

        return this.userService.getUserDetail(token).pipe(
            map(user => {
                if (user.role.name !== 'doctor') {
                    this.handleAuthFailure();
                    return false;
                }
                this.userService.saveUserResponse(user); 
                return true;
            }),
            catchError(() => {
                this.handleAuthFailure();
                return of(false);
            })
        );
    }

    private handleAuthFailure(): void {
        this.tokenService.clearToken();
        this.userService.clearUserInfo();
        alert('You are not authorized to access this page. Please log in again.');
        this.router.navigate(['/login'], { 
            queryParams: { 
                returnUrl: this.router.url,
                reason: 'session_expired' 
            }
        });
    }
}

export const AuthGuardProvider: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    return inject(DoctorGuard).canActivate();
};
