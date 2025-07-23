import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AdminLayoutRoutes } from './admin-layout.routing';
import { DashboardComponent } from '../../dashboard/dashboard.component';
//import { AddUserComponent } from '../../users/user-list/add-user/add-user.component';


import { ActualiteComponent } from '../../actualite/actualite.component';
import { EvenementComponent } from '../../evenements/evenement.component';
import { ReservationComponent } from '../../Reservations/reservation.component';
import { CalendarComponent } from '../../maps/calender.component';
//import { CandidaturesComponent } from '../../candidatures/candidatures.component';
import { UpgradeComponent } from '../../upgrade/upgrade.component';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatRippleModule} from '@angular/material/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSelectModule} from '@angular/material/select';

import { OffreComponent } from '../../offre/offre.component';
//import { AddoffreComponent } from '../../offre/addoffre/addoffre.component';



@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(AdminLayoutRoutes),
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatRippleModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
  ],
  declarations: [
    DashboardComponent,
    ActualiteComponent,
    EvenementComponent,
    ReservationComponent,
    CalendarComponent,
   // CandidaturesComponent,
    UpgradeComponent,
    OffreComponent,
   // AddoffreComponent
   
    
    
  ]

  
})

export class AdminLayoutModule {}
