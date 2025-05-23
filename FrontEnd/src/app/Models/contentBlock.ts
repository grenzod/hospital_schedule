export interface ContentBlock {
    type: 'text' | 'image';
    text: string;
    image: {
      url: string;
      caption: string;
    };
  }