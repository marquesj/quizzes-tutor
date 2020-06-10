import StatementOption from '@/models/statement/StatementOption';
import Image from '@/models/management/Image';
import { _ } from 'vue-underscore';

export default abstract class StatementQuestion {
  quizQuestionId!: number;
  content!: string;
  image: Image | null = null;
  type: string = "multiple_choice";

  constructor(jsonObj?: StatementQuestion) {
    if (jsonObj) {
      this.quizQuestionId = jsonObj.quizQuestionId;
      this.content = jsonObj.content;
      this.image = jsonObj.image;
      this.type = jsonObj.type;
    }
  }
}