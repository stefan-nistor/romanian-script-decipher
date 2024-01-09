import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Manuscript } from './manuscript.model';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ManuscriptService {
  path = 'http://localhost:8080/api';
  constructor(private router: Router, private http: HttpClient) {}

  getAllManuscripts(): Observable<Manuscript[]> {
    const x = this.path + '/profile/my-manuscripts/all';
    return this.http.get<Manuscript[]>(x);
  }

  getManuscript(id: string): Observable<Manuscript> {
    const x = this.path + `/profile/my-manuscripts/specific/${id}`;
    return this.http.get<Manuscript>(x);
  }

  addManuscript(manuscript: FormData): Observable<Manuscript> {
    const x = this.path + '/manuscript/decipher';
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', // Adjust the content type based on your API requirements
    });

    return this.http.post<Manuscript>(x, manuscript, { headers });
  }

  deleteManuscript(id: string): Observable<void> {
    const x = this.path + `/profile/my-manuscripts/delete/${id}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json', // Adjust the content type based on your API requirements
    });

    return this.http.delete<any>(x, { headers });
  }

  downloadManuscript(filename: string, manuscriptId: string): Observable<void> {
    const x =
      this.path + `/profile/my-manuscripts/download-original/${manuscriptId}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http
      .get<ArrayBuffer>(x, {
        headers,
        responseType: 'arraybuffer' as 'json',
      })
      .pipe(
        map((response: ArrayBuffer) => {
          console.log(filename);
          this.handleFileDownload(response, filename);
        })
      );
  }

  private handleFileDownload(response: ArrayBuffer, filename: string): void {
    const blob = new Blob([response], { type: 'application/octet-stream' });
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename; // Set the desired file name and extension
    document.body.appendChild(link);

    link.click();

    // Clean up
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);
  }
}
