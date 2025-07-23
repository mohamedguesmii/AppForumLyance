import { Routes } from '@angular/router';

import { DashboardComponent } from '../../dashboard/dashboard.component';
import { ActualiteComponent } from '../../actualite/actualite.component';
import { EvenementComponent } from '../../evenements/evenement.component';
import { ReservationComponent } from '../../Reservations/reservation.component';
import { CalendarComponent } from '../../maps/calender.component';
import { CandidaturesComponent } from '../../candidatures/candidatures.component';
//import { UpgradeComponent } from '../../upgrade/upgrade.component';
import { FileComponent } from 'app/file/file.component';
import { UserListComponent } from '../../users/user-list/user-list.component';
import { AddUserComponent } from '../../users/user-list/add-user/add-user.component';
import { OffreComponent } from '../../offre/offre.component';
import { AddoffreComponent } from 'app/offre/addoffre/addoffre.component';
import { ReservationStatsComponent } from 'app/reservation-stats/reservation-stats.component';
import { EvenementsvenirComponent } from 'app/evenementsvenir/evenementsvenir.component';
import { RendezVousComponent } from 'app/rendez-vous/rendez-vous.component';
import { ValidationsComponent } from 'app/validations/validations.component';
import { StatisticsComponent } from 'app/statistics/statistics.component';




export const AdminLayoutRoutes: Routes = [  
   
    { path: 'dashboard',                         component: DashboardComponent },
    { path: 'user',                              component: UserListComponent },
    { path: 'adduser',                           component: AddUserComponent },
    { path: 'statistiques',                component: StatisticsComponent },

    { path: 'Gestiondesactualites',              component: ActualiteComponent },
    { path: 'Gestiondesevenements',              component: EvenementComponent },
    { path: 'Evenementsvenir',                   component: EvenementsvenirComponent },

    { path: 'RendezVousComponent',                   component: RendezVousComponent },


    { path: 'Gestiondesreservations',            component: ReservationComponent },
    { path: 'reservation/statistiques', component: ReservationStatsComponent },

    { path: 'Gestiondesoffres',                  component: OffreComponent },
    { path: 'AddoffreComponent',                  component: AddoffreComponent },
    { path: 'Gestiondescandidatures',            component: CandidaturesComponent },
   // { path: 'upgrade',                           component: UpgradeComponent },
    { path: 'file',                              component: FileComponent },
    { path: 'upload',                            component: EvenementComponent },
    { path: 'validation',                            component: ValidationsComponent }



];
