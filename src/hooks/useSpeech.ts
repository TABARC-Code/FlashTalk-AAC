import { useCallback, useRef, useState } from 'react';
import * as Speech from 'expo-speech';

export interface SpeechSettings {
  rate: number;
  pitch: number;
}

const DEFAULT_SETTINGS: SpeechSettings = {
  rate: 0.95,
  pitch: 1.0,
};

/**
 * Wraps expo-speech so a single tap always interrupts any speech in
 * progress and speaks immediately — no queueing, no confirmation step.
 */
export function useSpeech() {
  const [settings, setSettings] = useState<SpeechSettings>(DEFAULT_SETTINGS);
  const speakingRef = useRef(false);

  const speak = useCallback(
    (phrase: string) => {
      Speech.stop();
      speakingRef.current = true;
      Speech.speak(phrase, {
        rate: settings.rate,
        pitch: settings.pitch,
        onDone: () => {
          speakingRef.current = false;
        },
        onStopped: () => {
          speakingRef.current = false;
        },
        onError: () => {
          speakingRef.current = false;
        },
      });
    },
    [settings]
  );

  const stop = useCallback(() => {
    Speech.stop();
    speakingRef.current = false;
  }, []);

  return { speak, stop, settings, setSettings };
}
