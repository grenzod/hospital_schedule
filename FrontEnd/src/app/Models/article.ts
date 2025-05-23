import { ContentBlock } from "./contentBlock";

export interface Article {
    id: string;
    title: string;
    thumbnail: string;
    contentBlocks: ContentBlock[];
    createdAt: Date
  }