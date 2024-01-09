import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Manuscript } from 'src/manuscript/manuscript.model';
import { ManuscriptService } from 'src/manuscript/manuscript.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html'
})
export class AccountComponent implements OnInit {
    tableData: Manuscript[] = [];
    constructor(private manuscriptService: ManuscriptService, private router: Router) {

    }
    ngOnInit(): void {
        this.manuscriptService.getAllManuscripts().subscribe(manuscripts => this.tableData = manuscripts);
    }
    
      viewItem(item: any): void {
        this.router.navigateByUrl(`/read/${item.id}`)
      }

    formatDate(date: Date): string {
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-based
        const year = date.getFullYear();
      
        return `${day}-${month}-${year}`;
      }

}