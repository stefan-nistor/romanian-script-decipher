import { Component } from "@angular/core";
import { Router } from "@angular/router";

@Component({
    selector: 'file-upload',
    templateUrl: './file-upload.component.html'
})
export class FileUploadComponent {
    constructor(private router: Router) {}
    Decypher() : void {
        this.router.navigateByUrl('/read');
    }
}