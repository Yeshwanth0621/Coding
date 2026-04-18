import './style.css';
import { SkyRogueGame } from './game';

const app = document.querySelector<HTMLDivElement>('#app');

if (!app) {
  throw new Error('App root #app was not found.');
}

app.innerHTML = `
  <div class="game-shell">
    <div id="viewport" class="viewport" aria-label="3D game viewport"></div>
    <div class="screen-noise" aria-hidden="true"></div>

    <div class="hud">
      <div class="hud-row hud-row--top">
        <section class="hud-card hud-card--brand">
          <p class="eyebrow">Sky Rogue // Aether Drift</p>
          <h1>Own the sky your way.</h1>
          <p class="subhead">Free-roam combat, crystal hunting, and boost-ring surfing across a custom-built floating frontier.</p>
        </section>

        <section class="hud-card hud-card--intel">
          <div class="stat-pill">
            <span>Score</span>
            <strong id="score-value">0</strong>
          </div>
          <div class="stat-pill">
            <span>Rank</span>
            <strong id="rank-value">Sky Cadet</strong>
          </div>
          <div class="stat-pill">
            <span>Combo</span>
            <strong id="combo-value">1.0x</strong>
          </div>
        </section>
      </div>

      <div class="hud-row hud-row--middle">
        <section class="hud-card hud-card--objectives">
          <p class="eyebrow">Mission Grid</p>
          <div class="objective-list">
            <div class="objective-item">
              <span>Harvest crystals</span>
              <strong id="objective-crystal">0 / 12</strong>
            </div>
            <div class="objective-item">
              <span>Disable drones</span>
              <strong id="objective-drone">0 / 6</strong>
            </div>
            <div class="objective-item">
              <span>Thread drift rings</span>
              <strong id="objective-ring">0 / 5</strong>
            </div>
          </div>
          <p id="mission-status" class="mission-status">No fixed route. Chase whatever feels good.</p>
        </section>

        <div class="reticle" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <i></i>
        </div>

        <section class="hud-card hud-card--radar">
          <div class="radar-head">
            <p class="eyebrow">Tactical Radar</p>
            <span id="sector-value">Citadel Rise</span>
          </div>
          <canvas id="radar" width="220" height="220" aria-label="Radar"></canvas>
          <p id="message-line" class="radar-message">Click launch and take the sky.</p>
        </section>
      </div>

      <div class="hud-row hud-row--bottom">
        <section class="hud-card hud-card--systems">
          <div class="meter-block">
            <div class="meter-label">
              <span>Hull</span>
              <strong id="speed-value">0 u/s</strong>
            </div>
            <div class="meter">
              <div id="hull-meter" class="meter-fill meter-fill--hull"></div>
            </div>
          </div>
          <div class="meter-block">
            <div class="meter-label">
              <span>Energy</span>
              <strong id="altitude-value">0 m</strong>
            </div>
            <div class="meter">
              <div id="energy-meter" class="meter-fill meter-fill--energy"></div>
            </div>
          </div>
          <div class="meter-block">
            <div class="meter-label">
              <span>Overdrive</span>
              <strong id="threat-value">Threat 0%</strong>
            </div>
            <div class="meter">
              <div id="boost-meter" class="meter-fill meter-fill--boost"></div>
            </div>
          </div>
        </section>

        <section class="hud-card hud-card--ledger">
          <div class="ledger-item">
            <span>Crystals</span>
            <strong id="crystal-count">0</strong>
          </div>
          <div class="ledger-item">
            <span>Drones</span>
            <strong id="drone-count">0</strong>
          </div>
          <div class="ledger-item">
            <span>Rings</span>
            <strong id="ring-count">0</strong>
          </div>
        </section>

        <section class="hud-card hud-card--controls">
          <p class="eyebrow">Flight Deck</p>
          <p>WASD strafe, mouse steer, Space climb, C dive, Shift overdrive.</p>
          <p>Left click fires, F or right click emits a pulse blast.</p>
        </section>
      </div>
    </div>

    <div id="toast" class="toast" role="status" aria-live="polite"></div>

    <section id="start-screen" class="overlay overlay--visible">
      <div class="overlay-panel">
        <p class="eyebrow">Custom Original 3D Game</p>
        <h2>Sky Rogue: Aether Drift</h2>
        <p>
          Tear through a glowing sky frontier, improvise your own route, and decide whether you want to hunt sentry drones,
          chain boost rings, or farm crystals for a massive score.
        </p>
        <div class="overlay-grid">
          <div>
            <span>Freedom</span>
            <strong>Roam anywhere in the archipelago</strong>
          </div>
          <div>
            <span>Flow</span>
            <strong>Boost, pulse, strafe, and surf momentum</strong>
          </div>
          <div>
            <span>Style</span>
            <strong>Hand-authored procedural art and HUD</strong>
          </div>
        </div>
        <button id="start-button" class="action-button" type="button">Launch Sky Run</button>
      </div>
    </section>

    <section id="game-over" class="overlay">
      <div class="overlay-panel overlay-panel--compact">
        <p class="eyebrow">Run Complete</p>
        <h2 id="final-rank">Sky Cadet</h2>
        <p id="final-brief">You made a dent in the frontier.</p>
        <div class="overlay-grid overlay-grid--compact">
          <div>
            <span>Final score</span>
            <strong id="final-score">0</strong>
          </div>
          <div>
            <span>Best move</span>
            <strong id="final-style">Crystal thief</strong>
          </div>
        </div>
        <button id="restart-button" class="action-button" type="button">Run It Back</button>
      </div>
    </section>
  </div>
`;

new SkyRogueGame(app);
