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
  OCR: string = '';
  NLP: string = '';
  currentJob: string = 'none';
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

  getOcr() : void {
    this.currentJob = 'OCR';
    this.manuscriptService.getOcr(this.manuscript.docId).subscribe(OCR => {
      this.OCR = OCR;
    });
  }

  getNlp() : void {
    this.currentJob = 'NLP';
    this.manuscriptService.getNlp(this.manuscript.docId).subscribe(NLP => {
      this.NLP = NLP;
    });
  }
}
