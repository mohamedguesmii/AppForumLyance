import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { Routes, RouterModule } from '@angular/router';

import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import {  EvenementComponent } from './evenements/evenement.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password/reset-password.component';
import { ChangerRoleComponent } from './users/user-list/changer-role/changer-role.component';
import { HomeComponent } from './layouts/front-office/frontoffice/home/home.component';
import { AuthGuard } from './auth-guard/auth-guard.component';
import { EntretienvisioComponent } from './layouts/front-office/frontoffice/entretienvisio/entretienvisio.component';
import { MatchingForumsComponent } from './layouts/front-office/frontoffice/matching-forums/matching-forums.component';
import { ValidationsComponent } from './validations/validations.component';
import { WorkflowMonitoringComponent } from './camunda/workflow-monitoring/workflow-monitoring.component';
import { ProcessusDetailsComponent } from './camunda/processus-details/processus-details.component';
import { HistoriqueProcessusComponent } from './camunda/historique-processus/historique-processus.component';



const routes: Routes = [


  {
  path: 'entretien/:token',
  component: EntretienvisioComponent,
  canActivate: [AuthGuard] // si tu utilises un guard
},

{
    path: 'MatchingForumsComponent',
    component: MatchingForumsComponent,
    canActivate: [AuthGuard]
  },


  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./layouts/admin-layout/admin-layout.module').then(m => m.AdminLayoutModule),
      },
    ],
  },
  {
    path: 'front',
    loadChildren: () =>
      import('./layouts/front-office/front-office.module').then(m => m.FrontOfficeModule),
  },
  { path: 'upload', component: EvenementComponent },
  { path: 'users', component: UserListComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'changer-role', component: ChangerRoleComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'matching-forums', component: MatchingForumsComponent },

  { path: 'validations', component: ValidationsComponent },
  { path: 'workflow-monitoring', component: WorkflowMonitoringComponent },
  { path: 'processus-details/:id', component: ProcessusDetailsComponent },
  { path: 'historique-processus', component: HistoriqueProcessusComponent },
  // Ajouter un wildcard pour redirection si besoin






];

@NgModule({
  imports: [
    CommonModule,
    BrowserModule,
    RouterModule.forRoot(routes, {
      useHash: true,
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
