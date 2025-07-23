import { Component } from '@angular/core';
import { ChatbotService } from 'app/services/chatbot.service';

interface Intent {
  name: string;
  keywords: string[];
  responses: string[];
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent {
  userId = 'user123';

  userQuestion = '';
  chatVisible = false;
  response: string | null = null;

  messages: { from: 'user' | 'bot'; text: string }[] = [];

  intents: Intent[] = [
    {
      name: 'forum',
      keywords: ['forum', 'forums', 'rencontre', 'discussion'],
      responses: [
        'Les forums ont lieu chaque trimestre, tu peux consulter le calendrier dans la section Forums.',
        'Pour participer à un forum, inscris-toi via la page dédiée.',
      ],
    },
    {
      name: 'offre',
      keywords: ['offre', 'emploi', 'stage', 'postuler', 'candidature'],
      responses: [
        'Les offres sont visibles dans la section Offres, n\'hésite pas à postuler directement en ligne.',
        'Tu peux filtrer les offres par domaine, type et durée.',
      ],
    },
    {
      name: 'evenement',
      keywords: ['événement', 'evenement', 'agenda', 'date', 'inscription'],
      responses: [
        'Les événements sont affichés dans la section Événements, pense à t\'inscrire tôt !',
        'Tu trouveras tous les détails et les dates dans l’agenda.',
      ],
    },
    {
      name: 'aide',
      keywords: ['aide', 'support', 'question', 'problème', 'contact'],
      responses: [
        'Je suis là pour t’aider ! Pose-moi ta question sur les forums, offres ou événements.',
        'Si tu as besoin d’assistance spécifique, contacte le support via la page Contact.',
      ],
    },
  ];

  constructor(private chatbotService: ChatbotService) {}

  toggleChat(): void {
    this.chatVisible = !this.chatVisible;
    this.response = null;
    this.userQuestion = '';
    this.messages = [];
  }

  sendQuestion(): void {
    if (!this.userQuestion.trim()) {
      alert('Merci de saisir une question.');
      return;
    }

    // Ajouter le message utilisateur dans l'historique
    this.messages.push({ from: 'user', text: this.userQuestion });

    // Réponse locale rapide (intent matching)
    const localResponse = this.getBestResponse(this.userQuestion);

    // On affiche la réponse locale d'abord
    this.response = localResponse;
    this.messages.push({ from: 'bot', text: localResponse });

    // Appeler le backend pour compléter/affiner la réponse
    this.chatbotService.sendMessage(this.userId, this.userQuestion).subscribe({
      next: (res: any) => {
        // Si backend renvoie plusieurs réponses textuelles
        if (res.responses && Array.isArray(res.responses)) {
          const backendResponses = res.responses
            .filter((r: any) => r.type === 'text' && r.text)
            .map((r: any) => r.text)
            .join('\n');

          if (backendResponses) {
            this.response += '\n' + backendResponses;
            this.messages.push({ from: 'bot', text: backendResponses });
          }
        } else if (res.response) {
          // backend renvoie une seule réponse
          this.response += '\n' + res.response;
          this.messages.push({ from: 'bot', text: res.response });
        }
      },
      error: (err) => {
        console.error(err);
        this.response += '\nErreur de communication avec le bot.';
        this.messages.push({ from: 'bot', text: 'Erreur de communication avec le bot.' });
      }
    });

    this.userQuestion = '';
  }

  getBestResponse(question: string): string {
    const qLower = question.toLowerCase();

    const scores = this.intents.map(intent => {
      const matched = intent.keywords.filter(kw => qLower.includes(kw));
      return { intent, score: matched.length };
    });

    const best = scores.reduce(
      (prev, current) => (current.score > prev.score ? current : prev),
      { intent: null as Intent | null, score: 0 }
    );

    if (best.score === 0 || !best.intent) {
      return "Désolé, je n'ai pas compris ta question. Essaie avec des mots comme 'forum', 'offre' ou 'événement'.";
    }

    const responses = best.intent.responses;
    return responses[Math.floor(Math.random() * responses.length)];
  }
}
