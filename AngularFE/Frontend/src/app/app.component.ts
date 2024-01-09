import { Component } from '@angular/core';
import { LoginService } from 'src/user/login/login.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Frontend';
  constructor(public loginService: LoginService) {}
  
  logout() : void {
    this.loginService.logout();
  }
}
