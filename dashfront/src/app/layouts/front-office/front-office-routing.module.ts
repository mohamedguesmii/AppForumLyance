import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FrontofficeComponent } from './frontoffice/frontoffice.component';
import { LoginComponent } from 'app/auth/login/login.component';
import { RegisterComponent } from 'app/auth/register/register.component';

import { HomeComponent } from './frontoffice/home/home.component';
import { AddComponent } from './frontoffice/candidature/add/add.component';
import { ReserverComponent } from './frontoffice/reserver/reserver.component';
import { EntretienvisioComponent } from './frontoffice/entretienvisio/entretienvisio.component';
import { MatchingForumsComponent } from './frontoffice/matching-forums/matching-forums.component';




export const routes: Routes = [



  {path:'',component:FrontofficeComponent},
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'addcandidature', component: AddComponent },
  { path: 'home', component: HomeComponent },
  { path: 'reserver/:id', component: ReserverComponent },
  { path: 'entretien/:token', component: EntretienvisioComponent },

  

];



@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FrontOfficeRoutingModule { }
