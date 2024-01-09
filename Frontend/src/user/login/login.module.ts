import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { LoginComponent } from "./login.component";
import { RegisterComponent } from "../register/register.component";
import { AccountComponent } from "../account/account.component";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: 'login',
                component: LoginComponent
            },
            {
                path: 'register',
                component: RegisterComponent
            },
            {
                path: 'account',
                component: AccountComponent
            }
        ])
    ],
    declarations : [
        LoginComponent,
        RegisterComponent,
        AccountComponent
    ]
})
export class LoginModule {}