import { Tile } from '../types';

export const tiles: Tile[] = [
  // Needs
  { id: 'need-water', label: 'Water', phrase: 'I want water', emoji: '💧', category: 'needs' },
  { id: 'need-food', label: 'Food', phrase: 'I want food', emoji: '🍽️', category: 'needs' },
  { id: 'need-bathroom', label: 'Bathroom', phrase: 'I need the bathroom', emoji: '🚻', category: 'needs' },
  { id: 'need-rest', label: 'Rest', phrase: 'I need to rest', emoji: '🛏️', category: 'needs' },
  { id: 'need-help', label: 'Help', phrase: 'I need help', emoji: '🆘', category: 'needs' },
  { id: 'need-break', label: 'Break', phrase: 'I need a break', emoji: '⏸️', category: 'needs' },

  // Feelings
  { id: 'feel-happy', label: 'Happy', phrase: 'I feel happy', emoji: '😀', category: 'feelings' },
  { id: 'feel-sad', label: 'Sad', phrase: 'I feel sad', emoji: '😢', category: 'feelings' },
  { id: 'feel-tired', label: 'Tired', phrase: 'I feel tired', emoji: '😴', category: 'feelings' },
  { id: 'feel-angry', label: 'Angry', phrase: 'I feel angry', emoji: '😠', category: 'feelings' },
  { id: 'feel-scared', label: 'Scared', phrase: 'I feel scared', emoji: '😨', category: 'feelings' },
  { id: 'feel-ok', label: 'OK', phrase: "I'm OK", emoji: '🙂', category: 'feelings' },

  // People
  { id: 'people-mom', label: 'Mom', phrase: 'Mom', emoji: '👩', category: 'people' },
  { id: 'people-dad', label: 'Dad', phrase: 'Dad', emoji: '👨', category: 'people' },
  { id: 'people-teacher', label: 'Teacher', phrase: 'Teacher', emoji: '🧑‍🏫', category: 'people' },
  { id: 'people-friend', label: 'Friend', phrase: 'Friend', emoji: '🧑‍🤝‍🧑', category: 'people' },
  { id: 'people-doctor', label: 'Doctor', phrase: 'Doctor', emoji: '🩺', category: 'people' },
  { id: 'people-me', label: 'Me', phrase: 'Me', emoji: '🙋', category: 'people' },

  // Social
  { id: 'social-yes', label: 'Yes', phrase: 'Yes', emoji: '✅', category: 'social' },
  { id: 'social-no', label: 'No', phrase: 'No', emoji: '❌', category: 'social' },
  { id: 'social-please', label: 'Please', phrase: 'Please', emoji: '🙏', category: 'social' },
  { id: 'social-thankyou', label: 'Thank you', phrase: 'Thank you', emoji: '💛', category: 'social' },
  { id: 'social-hello', label: 'Hello', phrase: 'Hello', emoji: '👋', category: 'social' },
  { id: 'social-bye', label: 'Bye', phrase: 'Goodbye', emoji: '👋', category: 'social' },

  // Actions
  { id: 'action-go', label: 'Go', phrase: "Let's go", emoji: '🚶', category: 'actions' },
  { id: 'action-stop', label: 'Stop', phrase: 'Stop', emoji: '✋', category: 'actions' },
  { id: 'action-more', label: 'More', phrase: 'More, please', emoji: '➕', category: 'actions' },
  { id: 'action-done', label: 'Done', phrase: "I'm done", emoji: '🏁', category: 'actions' },
  { id: 'action-play', label: 'Play', phrase: 'I want to play', emoji: '🎲', category: 'actions' },
  { id: 'action-look', label: 'Look', phrase: 'Look at this', emoji: '👀', category: 'actions' },
];
