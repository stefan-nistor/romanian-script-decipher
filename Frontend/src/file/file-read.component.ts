import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Manuscript } from "src/manuscript/manuscript.model";
import { ManuscriptService } from "src/manuscript/manuscript.service";

@Component({
    selector: 'file-read',
    templateUrl: './file-read.component.html'
})
export class FileReadComponent implements OnInit{
    manuscript: Manuscript | undefined;
    id = '';
    constructor(private manuscriptService: ManuscriptService, private route: ActivatedRoute) {
        
    }
    ngOnInit(): void {
        this.id = this.route.snapshot.params['manuscriptId'];
        this.manuscriptService.getManuscript(this.id).subscribe(
            manuscript => this.manuscript = manuscript
        );
    }
}