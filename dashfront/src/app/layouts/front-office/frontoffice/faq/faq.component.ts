import { Component, OnInit } from '@angular/core';

interface FaqItem {
  question: string;
  answer: string;
  open?: boolean;  // pour toggle affichage réponse
}

@Component({
  selector: 'app-faq',
  templateUrl: './faq.component.html',
  styleUrls: ['./faq.component.css']
})
export class FaqComponent implements OnInit {
errorMessage: any;
onSearch() {
throw new Error('Method not implemented.');
}

  faqs: FaqItem[] = [];
results: any;
query: any;
loading: any;

  constructor() { }

  ngOnInit(): void {
    this.faqs = [
      {
        question: "Comment postuler à un stage ?",
        answer: "Tu dois créer un compte, puis postuler via la page offres.",
        open: false
      },
      {
        question: "Quand ont lieu les forums ?",
        answer: "Les forums sont organisés plusieurs fois par an, généralement au printemps et en automne.",
        open: false
      },
      {
        question: "Qui peut participer au forum ?",
        answer: "Les candidats, les entreprises partenaires, et les responsables Lyance.",
        open: false
      }
    ];
  }

  toggleFaq(index: number): void {
    this.faqs[index].open = !this.faqs[index].open;
  }

}
