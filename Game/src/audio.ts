export class AudioDirector {
  private context: AudioContext | null = null;
  private masterGain: GainNode | null = null;
  private ambienceGain: GainNode | null = null;

  async start(): Promise<void> {
    if (!this.context) {
      this.context = new AudioContext();
      this.masterGain = this.context.createGain();
      this.masterGain.gain.value = 0.18;
      this.masterGain.connect(this.context.destination);

      this.ambienceGain = this.context.createGain();
      this.ambienceGain.gain.value = 0.045;
      this.ambienceGain.connect(this.masterGain);
      this.createAmbientBed();
    }

    if (this.context.state !== 'running') {
      await this.context.resume();
    }
  }

  shoot(): void {
    this.pitchSweep(280, 120, 0.12, 'sawtooth', 0.06);
  }

  pickup(): void {
    this.pitchSweep(660, 1080, 0.18, 'triangle', 0.08);
  }

  ring(): void {
    this.pitchSweep(300, 980, 0.28, 'triangle', 0.1);
  }

  pulse(): void {
    this.pitchSweep(180, 36, 0.42, 'sawtooth', 0.12);
  }

  damage(): void {
    this.pitchSweep(170, 70, 0.2, 'square', 0.08);
  }

  explosion(): void {
    this.pitchSweep(230, 40, 0.32, 'sawtooth', 0.13);
  }

  boost(): void {
    this.pitchSweep(410, 700, 0.14, 'triangle', 0.05);
  }

  private createAmbientBed(): void {
    if (!this.context || !this.ambienceGain) {
      return;
    }

    const frequencies = [72, 108];
    for (const frequency of frequencies) {
      const oscillator = this.context.createOscillator();
      oscillator.type = 'triangle';
      oscillator.frequency.value = frequency;

      const gain = this.context.createGain();
      gain.gain.value = frequency === 72 ? 0.7 : 0.42;

      oscillator.connect(gain);
      gain.connect(this.ambienceGain);
      oscillator.start();
    }

    const lfo = this.context.createOscillator();
    lfo.type = 'sine';
    lfo.frequency.value = 0.08;

    const lfoGain = this.context.createGain();
    lfoGain.gain.value = 0.012;
    lfo.connect(lfoGain);
    lfoGain.connect(this.ambienceGain.gain);
    lfo.start();
  }

  private pitchSweep(
    startFrequency: number,
    endFrequency: number,
    duration: number,
    type: OscillatorType,
    level: number,
  ): void {
    if (!this.context || !this.masterGain) {
      return;
    }

    const now = this.context.currentTime;
    const oscillator = this.context.createOscillator();
    oscillator.type = type;
    oscillator.frequency.setValueAtTime(startFrequency, now);
    oscillator.frequency.exponentialRampToValueAtTime(
      Math.max(24, endFrequency),
      now + duration,
    );

    const gain = this.context.createGain();
    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.exponentialRampToValueAtTime(level, now + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + duration);

    oscillator.connect(gain);
    gain.connect(this.masterGain);
    oscillator.start(now);
    oscillator.stop(now + duration + 0.02);
  }
}
