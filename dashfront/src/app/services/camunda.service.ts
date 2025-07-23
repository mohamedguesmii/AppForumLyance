import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Types généraux pour tâches, forums, offres, candidatures

export type NomFormulaireType = 'forum' | 'offre' | 'candidature' | string;

export type StatutType = 'VALIDÉ' | 'REFUSÉ' | 'EN_ATTENTE' | string;

export interface Task {
  type: string;
  id: string;
  name: string;
  nomFormulaire?: NomFormulaireType;
  eventTitle?: string;
  statut?: StatutType;
  processInstanceId?: string;
  variables?: Record<string, any>;
    formKey?: string; // ✅ Ajouté

}

export interface Forum {
  id: number;
  titre: string;
  statut: StatutType;
  dateDerniereAction: Date | string;
}

export interface Offre {
  id: number;
  titre: string;
  statut: StatutType;
  dateDerniereAction: Date | string;
}

export interface Candidature {
  id: number;
  titre: string;
  statut: StatutType;
  dateDerniereAction: Date | string;
}

export interface ProcessStartPayload {
  id: number;
  nomEvenement: string;
  type: 'forum' | 'candidature' | 'offre';
}

export interface Status {
  type: 'forum' | 'offre' | 'candidature';
  dateDerniereAction?: string | null;
  forumId?: number;
  offreId?: number;
  candidatureId?: number;
  titre: string;
  processInstanceId: string;
  statut: StatutType;
}

export interface HistoricProcessInstance {
  id: string;
  processDefinitionId: string;
  processDefinitionKey: string;
  businessKey?: string;
  startTime?: string;
  endTime?: string;
  state: 'COMPLETED' | 'RUNNING' | string;
}

@Injectable({
  providedIn: 'root'
})
export class CamundaService {

  private readonly apiBaseUrl = 'http://localhost:8089/api';
  getBpmnXml: any;

  constructor(private http: HttpClient) {}

  // --- Gestion des tâches ---

  /**
   * Récupère toutes les tâches assignées au service RH
   * Doit retourner offres + candidatures + forums
   */
  getTasksForRh(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiBaseUrl}/forum/tasks/rh`);
  }

  /**
   * Récupère toutes les tâches assignées à un utilisateur donné
   */
  getTasksForUser(username: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiBaseUrl}/forum/tasks/user/${username}`);
  }

  /**
   * Complète une tâche Camunda en mettant à jour son statut
   * Statut doit être 'VALIDÉ', 'REFUSÉ' ou 'EN_ATTENTE'
   */
  completeTask(taskId: string, statut: StatutType = 'VALIDÉ'): Observable<void> {
    return this.http.post<void>(`${this.apiBaseUrl}/forum/tasks/complete/${taskId}`, { statut });
  }

  // --- Gestion des entités historiques ---

  getAllForums(): Observable<Forum[]> {
    return this.http.get<Forum[]>(`${this.apiBaseUrl}/forum/forums/historique`);
  }

  deleteForum(forumId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/forum/forums/${forumId}`);
  }

  getAllOffres(): Observable<Offre[]> {
    return this.http.get<Offre[]>(`${this.apiBaseUrl}/offres/historique`);
  }

  deleteOffre(offreId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/offres/${offreId}`);
  }

  getAllCandidatures(): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.apiBaseUrl}/candidatures/historique`);
  }

  deleteCandidature(candidatureId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/candidatures/${candidatureId}`);
  }

  // --- Gestion des statuts des processus ---

  getAllStatuses(): Observable<Status[]> {
    return this.http.get<Status[]>(`${this.apiBaseUrl}/forum/statuses`);
  }

  // --- Démarrage des processus Camunda (validation) ---

  demarrerValidation(payload: ProcessStartPayload): Observable<void> {
    return this.http.post<void>(`${this.apiBaseUrl}/forum/demarrer-validation`, payload);
  }

  // --- Gestion des processus Camunda ---

  deleteProcessInstance(processInstanceId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/forum/process-instance/${processInstanceId}`);
  }

  // --- Historique Camunda ---

  getAllHistoricProcessInstances(page = 0, size = 10): Observable<HistoricProcessInstance[]> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<HistoricProcessInstance[]>(`${this.apiBaseUrl}/camunda/history/all`, { params });
  }

  getHistoricProcessInstanceById(id: string): Observable<HistoricProcessInstance> {
    return this.http.get<HistoricProcessInstance>(`${this.apiBaseUrl}/camunda/history/${id}`);
  }

  getCompletedHistoricProcessInstances(
    page = 0,
    size = 10,
    startedAfter?: string,
    startedBefore?: string
  ): Observable<HistoricProcessInstance[]> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (startedAfter) params = params.set('startedAfter', startedAfter);
    if (startedBefore) params = params.set('startedBefore', startedBefore);
    return this.http.get<HistoricProcessInstance[]>(`${this.apiBaseUrl}/camunda/history/completed`, { params });
  }

  getTaskVariables(taskId: string): Observable<{ [key: string]: { value: any; type: string } }> {
  return this.http.get<{ [key: string]: { value: any; type: string } }>(`${this.apiBaseUrl}/forum/tasks/${taskId}/variables`);
}

}
