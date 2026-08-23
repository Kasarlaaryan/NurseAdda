/**
 * Plays a subtle notification chime using the Web Audio API.
 * No external audio files required — generates a soft two-tone "ding" synthetically.
 */
let audioContext: AudioContext | null = null;

function getAudioContext(): AudioContext {
  if (!audioContext) {
    audioContext = new AudioContext();
  }
  return audioContext;
}

/**
 * Play a soft two-tone notification chime.
 * Safe to call multiple times — will create a fresh context if the previous one was closed.
 */
export function playNotificationSound(): void {
  try {
    const ctx = getAudioContext();

    // If context was suspended (browser autoplay policy), resume it
    if (ctx.state === 'suspended') {
      ctx.resume();
    }

    const now = ctx.currentTime;

    // First tone — warm "ding"
    const osc1 = ctx.createOscillator();
    const gain1 = ctx.createGain();
    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(880, now); // A5
    gain1.gain.setValueAtTime(0, now);
    gain1.gain.linearRampToValueAtTime(0.15, now + 0.02); // quick attack
    gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.4); // decay
    osc1.connect(gain1);
    gain1.connect(ctx.destination);
    osc1.start(now);
    osc1.stop(now + 0.4);

    // Second tone — higher, softer "ting"
    const osc2 = ctx.createOscillator();
    const gain2 = ctx.createGain();
    osc2.type = 'sine';
    osc2.frequency.setValueAtTime(1175, now + 0.12); // D6
    gain2.gain.setValueAtTime(0, now + 0.12);
    gain2.gain.linearRampToValueAtTime(0.08, now + 0.14);
    gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.5);
    osc2.connect(gain2);
    gain2.connect(ctx.destination);
    osc2.start(now + 0.12);
    osc2.stop(now + 0.5);
  } catch {
    // Web Audio not supported or blocked — fail silently
  }
}
