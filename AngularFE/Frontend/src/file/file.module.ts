import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { FileUploadComponent } from "./file-upload.component";
import { FileReadComponent } from "./file-read.component";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild([
            {
                path: 'upload',
                component: FileUploadComponent
            },
            {
                path: 'read',
                component: FileReadComponent
            }
        ])
    ],
    declarations : [
        FileUploadComponent,
        FileReadComponent
    ]
})
export class FileModule {}