import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Manuscript } from "./manuscript.model";
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root',
  })
export class ManuscriptService {
    path = "http://localhost:8080/api"
    manuscripts: Manuscript[] = [
        {
            yearOfPublication: 1990, 
            titleOfManuscript: 'Document1', 
            author: 'Author1',
            id: '1234-1234-1234-1235'
        }, 
        {
            yearOfPublication: 1991, 
            titleOfManuscript: 'Document2', 
            author: 'Author2',
            id: '1234-1234-1234-1235'
        }
    ];
    constructor(private router: Router, private http: HttpClient) {

    }

    getAllManuscripts() : Observable<Manuscript[]> {
        this.path = this.path + '/profile/my-manuscripts/all';
        return this.http.get<Manuscript[]>(this.path);
    }

    getManuscript(id: string) : Observable<Manuscript> {
        this.path = this.path + '/profile/my-manuscripts/specific';
        return this.http.get<Manuscript>(this.path);
    }

    addManuscript(manuscript: FormData) : Observable<Manuscript> {
        this.path = this.path + '/manuscript/decipher';
        const headers = new HttpHeaders({
            'Content-Type': 'application/json', // Adjust the content type based on your API requirements
          });

          return this.http.post<Manuscript>(this.path, manuscript, { headers } );
    }
}