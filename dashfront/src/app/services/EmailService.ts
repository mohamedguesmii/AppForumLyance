import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmailRequest } from 'app/models/EmailRequest';

@Injectable({
  providedIn: 'root'
})
export class EmailService {

  private apiUrl = 'http://localhost:8089/api/email/send';

  constructor(private http: HttpClient) {}

  // Précise bien que la réponse est un objet (pas juste string) si c'est le cas, sinon string c'est ok.
 sendEmail(emailRequest: EmailRequest): Observable<{message?: string; error?: string}> {
  return this.http.post<{message?: string; error?: string}>(this.apiUrl, emailRequest);
}

}
export { EmailRequest };

