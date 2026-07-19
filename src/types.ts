export type CategoryId = 'needs' | 'feelings' | 'people' | 'social' | 'actions';

export interface Category {
  id: CategoryId;
  label: string;
  color: string;
}

export interface Tile {
  id: string;
  label: string;
  /** Text spoken aloud when the tile is tapped. Usually equal to `label`. */
  phrase: string;
  emoji: string;
  category: CategoryId;
}
