import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { FileUploadComponent } from "./file-upload.component";
import { FileReadComponent } from "./file-read.component";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild([
            {
                path: 'upload',
                component: FileUploadComponent
            },
            {
                path: 'read/:manuscriptId',
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