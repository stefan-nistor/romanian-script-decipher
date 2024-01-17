import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Manuscript } from 'src/manuscript/manuscript.model';
import { ManuscriptService } from 'src/manuscript/manuscript.service';
import { Router } from '@angular/router';
import { interval, switchMap, takeWhile } from 'rxjs';

@Component({
  selector: 'file-read',
  templateUrl: './file-read.component.html',
})
export class FileReadComponent implements OnInit {
  manuscript!: Manuscript;
  translation: string = '';
  id = '';
  constructor(
    private manuscriptService: ManuscriptService,
    private route: ActivatedRoute,
    private router: Router
  ) {}
  ngOnInit(): void {
    this.id = this.route.snapshot.params['manuscriptId'];
    this.manuscriptService
      .getManuscript(this.id)
      .subscribe(manuscript => 
        {
          this.manuscript = manuscript;
          this.manuscriptService.getTranslation(manuscript.docId).subscribe(translation => {
            this.translation = translation;
          })
        });
  }

  deleteManuscript(): void {
    this.manuscriptService
      .deleteManuscript(this.manuscript.manuscriptId)
      .subscribe(
        () => {
          console.log('File deleted successfully');
          this.router.navigateByUrl('');
        },
        (error: any) => {
          console.error('File delete failed', error);
        }
      );
  }

  downloadManuscript(): void {
    console.log(this.manuscript);
    this.manuscriptService
      .downloadManuscript(
        this.manuscript.filename,
        this.manuscript.manuscriptId
      )
      .subscribe(
        () => {
          console.log('File downloaded successfully');
        },
        (error: any) => {
          console.error('File download failed', error);
        }
      );
  }
}
