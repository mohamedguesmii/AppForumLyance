import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FrontOfficeRoutingModule } from './front-office-routing.module';
import { FrontofficeComponent } from './frontoffice/frontoffice.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ComponentsModule } from "../../components/components.module";
import { CandidatureComponent } from './frontoffice/candidature/candidature.component';
import { HomeComponent } from './frontoffice/home/home.component';
import { MatIconModule } from '@angular/material/icon';
import { AddComponent } from './frontoffice/candidature/add/add.component';
import { ReserverComponent } from './frontoffice/reserver/reserver.component';
import { RendezzVousComponent } from './frontoffice/rendezz-vous/rendezz-vous.component';
import { EntretienvisioComponent } from './frontoffice/entretienvisio/entretienvisio.component';
import { FaqComponent } from './frontoffice/faq/faq.component';
import { RagSearchComponent } from './frontoffice/rag-search/rag-search.component';
import { ChatbotComponent } from './frontoffice/chatbot/chatbot.component';
import { MatchingForumsComponent } from './frontoffice/matching-forums/matching-forums.component';


@NgModule({
  declarations: [
    FrontofficeComponent,
    CandidatureComponent,
   HomeComponent,
   AddComponent,
   ReserverComponent,
   RendezzVousComponent,
   EntretienvisioComponent,
   FaqComponent,
   RagSearchComponent,
   ChatbotComponent,
   MatchingForumsComponent,
   

  
  ],
  imports: [
    CommonModule,
    FrontOfficeRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ComponentsModule,
     MatIconModule // ✅ Ajout nécessaire

  ]
})
export class FrontOfficeModule { }
