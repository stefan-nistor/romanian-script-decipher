import { HttpClient } from "@angular/common/http";
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { Manuscript } from "src/manuscript/manuscript.model";
import { ManuscriptService } from "src/manuscript/manuscript.service";

@Component({
    selector: 'file-upload',
    templateUrl: './file-upload.component.html'
})
export class FileUploadComponent {

    uploadForm!: FormGroup;

    constructor(private fb: FormBuilder, private router: Router, private manuscriptService: ManuscriptService, private http: HttpClient) { }

    ngOnInit(): void {
        this.uploadForm = this.fb.group({
            uploadedFile: [null, [Validators.required]],
            titleOfManuscript: ['', [Validators.required]],
            author: ['', [Validators.required]],
            description: ['', [Validators.required]],
            yearOfPublication: ['', [Validators.required]]
        });
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        this.uploadForm.patchValue({ file });
        this.uploadForm.get('uploadedFile')?.updateValueAndValidity();
      }

    Decypher() : void {
        if (this.uploadForm.valid) {
            const formData = new FormData();
            formData.append('manuscript', this.uploadForm.get('uploadedFile')?.value);
            const manuscript = {
                titleOfManuscript: '',
                author: '',
                yearOfPublication: 1,
                description: ''
            }
            formData.append('manuscriptDetails', JSON.stringify(manuscript));
            this.manuscriptService.addManuscript(formData).subscribe(
                next => console.log(next)
            );
            

            }
        }
    }