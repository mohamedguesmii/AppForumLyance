import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Task {
  id: string;
  name: string;
  eventTitle: string;
  statut: string;
  processInstanceId?: string;
  variables?: any;
}

export interface Forum {
  id: string;
  titre: string;
  statut: string;
  dateDerniereAction: Date | string;
}

@Injectable({
  providedIn: 'root'
})
export class ForumWorkflowService {

  private apiUrl = 'http://localhost:8089/api/forum';

  constructor(private http: HttpClient) {}

  getTasksForRh(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/rh`);
  }

  completeTask(taskId: string, statut: string): Observable<any> {
    // Envoie { statut: 'VALIDÉ' } ou { statut: 'REFUSÉ' } au backend
    return this.http.post(`${this.apiUrl}/tasks/complete/${taskId}`, { statut });
  }

  deleteProcessInstance(processInstanceId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/process-instance/${processInstanceId}`);
  }

  getAllStatuses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/statuses`);
  }

  getForumsHistorique(): Observable<Forum[]> {
    return this.http.get<Forum[]>(`${this.apiUrl}/forums/historique`);
  }

  deleteForum(forumId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/forums/${forumId}`);
  }

  demarrerValidationForum(forumId: number, nomEvenement: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/demarrer-validation`, { forumId, nomEvenement });
  }
}
