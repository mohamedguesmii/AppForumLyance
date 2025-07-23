import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule, LOCALE_ID } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app.routing';
import { ComponentsModule } from './components/components.module';

import { AppComponent } from './app.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { FileComponent } from './file/file.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password/reset-password.component';
import { AddUserComponent } from './users/user-list/add-user/add-user.component';
import { ChangerRoleComponent } from './users/user-list/changer-role/changer-role.component';
import { CandidaturesComponent } from './candidatures/candidatures.component';
import { AddoffreComponent } from './offre/addoffre/addoffre.component';

import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthInterceptor } from 'app/users/user-list/auth.interceptor'; // Ajuste le chemin selon ton projet

import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { ReservationStatsComponent } from './reservation-stats/reservation-stats.component';
import { EvenementsvenirComponent } from './evenementsvenir/evenementsvenir.component';
import { RendezVousComponent } from './rendez-vous/rendez-vous.component';
import { MatchingForumsComponent } from './layouts/front-office/frontoffice/matching-forums/matching-forums.component';
import { ValidationsComponent } from './validations/validations.component';
import { StatisticsComponent } from './statistics/statistics.component';
import { WorkflowMonitoringComponent } from './camunda/workflow-monitoring/workflow-monitoring.component';
import { ProcessusDetailsComponent } from './camunda/processus-details/processus-details.component';
import { NgChartsModule } from 'ng2-charts';
import { HistoriqueProcessusComponent } from './camunda/historique-processus/historique-processus.component';

// Enregistre la locale française
registerLocaleData(localeFr);

@NgModule({
  imports: [
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ComponentsModule,
    RouterModule,
    AppRoutingModule,
    MatSnackBarModule,
    NgChartsModule,  // <--- Ajouter ici

    
    
    
  ],
  declarations: [
    AppComponent,
    AdminLayoutComponent,
    FileComponent,
    UserListComponent,
    LoginComponent,
    RegisterComponent,
    ResetPasswordComponent,
    ForgotPasswordComponent,
    AddUserComponent,
    ChangerRoleComponent,
    CandidaturesComponent,
    AddoffreComponent,
    ReservationStatsComponent,
    EvenementsvenirComponent,
    RendezVousComponent,
    ValidationsComponent,
    StatisticsComponent,
    WorkflowMonitoringComponent,
    ProcessusDetailsComponent,
    HistoriqueProcessusComponent,    
    
    
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    { provide: LOCALE_ID, useValue: 'fr-FR' }  // <-- Ajout de la locale française ici
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
