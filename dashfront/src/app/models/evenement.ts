import { Image } from "./Image";

export interface Evenement {
  titre: string;
  id: number;
  date: string | number | Date;
  image: string | ArrayBuffer;
  body: Evenement;
  name: any;
  comments: any;
  likes: any;
    

    idevent : number ;
    title : string;
    description : string;
    capacity :number;
    status:string;
    datedebut: Date ;
    datefin: Date;
    adresse: string;
    imageUrl: String;
    starRating:number;
    imagecloud?: Image

    // Champ pour différencier les événements scrappés
  isScraped?: boolean;
}
 