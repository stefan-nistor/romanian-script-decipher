import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Manuscript } from 'src/manuscript/manuscript.model';
import { ManuscriptService } from 'src/manuscript/manuscript.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
})
export class AccountComponent implements OnInit {
  tableData: Manuscript[] = [];
  constructor(
    private manuscriptService: ManuscriptService,
    private router: Router
  ) {}
  ngOnInit(): void {
    this.manuscriptService
      .getAllManuscripts()
      .subscribe((manuscripts) => (this.tableData = manuscripts));
  }

  viewItem(item: Manuscript): void {
    this.router.navigateByUrl(`/read/${item.manuscriptId}`);
  }
}
