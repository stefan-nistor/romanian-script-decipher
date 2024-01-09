import { Injectable } from "@angular/core";
import { Router } from "@angular/router";

@Injectable({
    providedIn: 'root',
  })
export class LoginService {
    isLoggedIn = false;
    constructor(private router: Router) {

    }

    login(username: string, password: string): void {
        //logic for login
        this.isLoggedIn = true;
        this.router.navigateByUrl('');
    }

    logout() : void {
        this.isLoggedIn = false;
        this.router.navigateByUrl('');
    }
}