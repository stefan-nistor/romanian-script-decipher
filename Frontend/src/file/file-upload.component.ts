import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Manuscript } from 'src/manuscript/manuscript.model';
import { ManuscriptService } from 'src/manuscript/manuscript.service';

@Component({
  selector: 'file-upload',
  templateUrl: './file-upload.component.html',
})
export class FileUploadComponent {
  uploadForm!: FormGroup;
  pdfSrc = '/CV_Ștefan_Roman.pdf';
  selectedFile!: File;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private manuscriptService: ManuscriptService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.uploadForm = this.fb.group({
      uploadedFile: [null, [Validators.required]],
      titleOfManuscript: ['', [Validators.required]],
      author: ['', [Validators.required]],
      description: ['', [Validators.required]],
      yearOfPublication: ['', [Validators.required]],
    });
  }

  onFileSelected(event: any): void {
    // const file = event.target.files[0];
    // this.uploadForm.patchValue({ file });
    // this.uploadForm.get('uploadedFile')?.updateValueAndValidity();

    this.selectedFile = event.target.files[0];
  }

  Decypher(): void {
    if (this.uploadForm.valid) {
      const formData = new FormData();
      formData.append('manuscript', this.selectedFile);
      const manuscript = {
        title: this.uploadForm.get('titleOfManuscript')?.value,
        author: this.uploadForm.get('author')?.value,
        yearOfPublication: this.uploadForm.get('yearOfPublication')?.value,
        description: this.uploadForm.get('description')?.value,
      };
      formData.append('manuscriptDetails', JSON.stringify(manuscript));

      console.log(formData);
      this.manuscriptService.addManuscript(formData).subscribe((next) => {
        // endpointul de addManuscript trebuie sa imi returneze manuscriptul inapoi,
        // sau at least id-ul lui, sa pot face redirect
        this.manuscriptService
          .getManuscript(next.manuscriptId)
          .subscribe((manuscript) => {
            this.router.navigateByUrl(`/read/${manuscript.manuscriptId}`);
          });
      });
    }
  }
}
