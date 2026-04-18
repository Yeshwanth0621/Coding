import * as THREE from 'three';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { AudioDirector } from './audio';

const CRYSTAL_TARGET = 12;
const DRONE_TARGET = 6;
const RING_TARGET = 5;
const WORLD_LIMIT = 760;
const PLAYER_RADIUS = 7;
const PLAYER_CRUISE_SPEED = 96;
const PLAYER_BOOST_SPEED = 154;
const TAU = Math.PI * 2;

interface HudRefs {
  startScreen: HTMLElement;
  gameOver: HTMLElement;
  startButton: HTMLButtonElement;
  restartButton: HTMLButtonElement;
  toast: HTMLElement;
  scoreValue: HTMLElement;
  rankValue: HTMLElement;
  comboValue: HTMLElement;
  objectiveCrystal: HTMLElement;
  objectiveDrone: HTMLElement;
  objectiveRing: HTMLElement;
  missionStatus: HTMLElement;
  messageLine: HTMLElement;
  sectorValue: HTMLElement;
  speedValue: HTMLElement;
  altitudeValue: HTMLElement;
  threatValue: HTMLElement;
  hullMeter: HTMLElement;
  energyMeter: HTMLElement;
  boostMeter: HTMLElement;
  crystalCount: HTMLElement;
  droneCount: HTMLElement;
  ringCount: HTMLElement;
  radar: HTMLCanvasElement;
  finalScore: HTMLElement;
  finalRank: HTMLElement;
  finalBrief: HTMLElement;
  finalStyle: HTMLElement;
}

interface GeneratedTextures {
  terrain: THREE.CanvasTexture;
  rock: THREE.CanvasTexture;
  glow: THREE.CanvasTexture;
  ember: THREE.CanvasTexture;
  cloud: THREE.CanvasTexture;
}

interface Island {
  name: string;
  group: THREE.Group;
  position: THREE.Vector3;
  radius: number;
  colliderCenter: THREE.Vector3;
  colliderRadius: number;
}

interface Crystal {
  group: THREE.Group;
  basePosition: THREE.Vector3;
  bobOffset: number;
  active: boolean;
  cooldown: number;
}

interface Drone {
  group: THREE.Group;
  eye: THREE.Sprite;
  velocity: THREE.Vector3;
  anchor: THREE.Vector3;
  phase: number;
  alive: boolean;
  health: number;
  cooldown: number;
  respawn: number;
}

interface Ring {
  group: THREE.Group;
  halo: THREE.Mesh;
  radius: number;
  cooldown: number;
  active: boolean;
  pulse: number;
  laneName: string;
}

interface Projectile {
  sprite: THREE.Sprite;
  velocity: THREE.Vector3;
  life: number;
  damage: number;
  radius: number;
  friendly: boolean;
}

interface Particle {
  mesh: THREE.Mesh;
  velocity: THREE.Vector3;
  life: number;
  maxLife: number;
  growth: number;
}

interface Cloud {
  sprite: THREE.Sprite;
  base: THREE.Vector3;
  drift: number;
  bob: number;
}

function clamp01(value: number): number {
  return THREE.MathUtils.clamp(value, 0, 1);
}

function axisFromKeys(keys: Set<string>, positive: string, negative: string): number {
  return (keys.has(positive) ? 1 : 0) - (keys.has(negative) ? 1 : 0);
}

function requireElement<T extends Element>(root: ParentNode, selector: string): T {
  const element = root.querySelector<T>(selector);
  if (!element) {
    throw new Error(`Required element not found: ${selector}`);
  }
  return element;
}

function hash2d(x: number, y: number, seed: number): number {
  const value = Math.sin(x * 127.1 + y * 311.7 + seed * 74.7) * 43758.5453123;
  return value - Math.floor(value);
}

function valueNoise(x: number, y: number, seed: number): number {
  const ix = Math.floor(x);
  const iy = Math.floor(y);
  const fx = x - ix;
  const fy = y - iy;
  const sx = fx * fx * (3 - 2 * fx);
  const sy = fy * fy * (3 - 2 * fy);

  const n00 = hash2d(ix, iy, seed);
  const n10 = hash2d(ix + 1, iy, seed);
  const n01 = hash2d(ix, iy + 1, seed);
  const n11 = hash2d(ix + 1, iy + 1, seed);

  const nx0 = THREE.MathUtils.lerp(n00, n10, sx);
  const nx1 = THREE.MathUtils.lerp(n01, n11, sx);
  return THREE.MathUtils.lerp(nx0, nx1, sy);
}

function fbm(x: number, y: number, seed: number): number {
  let total = 0;
  let amplitude = 0.55;
  let frequency = 0.65;
  let sum = 0;

  for (let octave = 0; octave < 4; octave += 1) {
    total += valueNoise(x * frequency, y * frequency, seed + octave * 11.7) * amplitude;
    sum += amplitude;
    amplitude *= 0.5;
    frequency *= 2.1;
  }

  return total / sum;
}

function createTexture(
  size: number,
  draw: (ctx: CanvasRenderingContext2D, size: number) => void,
): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const context = canvas.getContext('2d');
  if (!context) {
    throw new Error('2D canvas context is unavailable.');
  }

  draw(context, size);
  const texture = new THREE.CanvasTexture(canvas);
  texture.wrapS = THREE.RepeatWrapping;
  texture.wrapT = THREE.RepeatWrapping;
  texture.colorSpace = THREE.SRGBColorSpace;
  return texture;
}

function createGlowTexture(innerColor: string, outerColor: string): THREE.CanvasTexture {
  return createTexture(256, (ctx, size) => {
    const gradient = ctx.createRadialGradient(size / 2, size / 2, size * 0.05, size / 2, size / 2, size * 0.5);
    gradient.addColorStop(0, innerColor);
    gradient.addColorStop(0.24, innerColor);
    gradient.addColorStop(0.58, outerColor);
    gradient.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, size, size);
  });
}

function createTerrainTexture(
  baseColor: string,
  accentColor: string,
  highlightColor: string,
): THREE.CanvasTexture {
  return createTexture(256, (ctx, size) => {
    const gradient = ctx.createLinearGradient(0, 0, size, size);
    gradient.addColorStop(0, baseColor);
    gradient.addColorStop(1, accentColor);
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, size, size);

    for (let index = 0; index < 2200; index += 1) {
      const x = Math.random() * size;
      const y = Math.random() * size;
      const noise = fbm(x * 0.08, y * 0.08, 9.3);
      const alpha = 0.04 + noise * 0.09;
      ctx.fillStyle = index % 4 === 0 ? `rgba(255,255,255,${alpha})` : `rgba(0,0,0,${alpha * 0.7})`;
      const dimension = 2 + Math.random() * 8;
      ctx.fillRect(x, y, dimension, dimension);
    }

    ctx.globalCompositeOperation = 'screen';
    ctx.fillStyle = highlightColor;
    ctx.globalAlpha = 0.12;
    ctx.fillRect(0, 0, size, size);
    ctx.globalAlpha = 1;
    ctx.globalCompositeOperation = 'source-over';
  });
}

function createCloudTexture(): THREE.CanvasTexture {
  return createTexture(256, (ctx, size) => {
    const gradient = ctx.createRadialGradient(size * 0.42, size * 0.46, size * 0.08, size / 2, size / 2, size * 0.48);
    gradient.addColorStop(0, 'rgba(255,255,255,0.98)');
    gradient.addColorStop(0.38, 'rgba(194,255,255,0.62)');
    gradient.addColorStop(1, 'rgba(255,255,255,0)');
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, size, size);
  });
}

export class SkyRogueGame {
  private readonly shell: HTMLElement;
  private readonly viewport: HTMLElement;
  private readonly hud: HudRefs;
  private readonly radarContext: CanvasRenderingContext2D;
  private readonly scene = new THREE.Scene();
  private readonly camera = new THREE.PerspectiveCamera(62, 1, 0.1, 2200);
  private readonly renderer = new THREE.WebGLRenderer({
    antialias: true,
    powerPreference: 'high-performance',
  });
  private readonly composer: EffectComposer;
  private readonly bloomPass: UnrealBloomPass;
  private readonly clock = new THREE.Clock();
  private readonly audio = new AudioDirector();
  private readonly world = new THREE.Group();
  private readonly textures: GeneratedTextures;
  private readonly particleGeometry = new THREE.SphereGeometry(1, 7, 7);
  private readonly pointer = new THREE.Vector2();
  private readonly lookTarget = new THREE.Vector3();
  private readonly tempA = new THREE.Vector3();
  private readonly tempB = new THREE.Vector3();
  private readonly tempC = new THREE.Vector3();
  private readonly tempQuaternion = new THREE.Quaternion();
  private readonly islands: Island[] = [];
  private readonly crystals: Crystal[] = [];
  private readonly drones: Drone[] = [];
  private readonly rings: Ring[] = [];
  private readonly projectiles: Projectile[] = [];
  private readonly particles: Particle[] = [];
  private readonly clouds: Cloud[] = [];
  private readonly playerGroup = new THREE.Group();
  private readonly ship = new THREE.Group();
  private readonly noseAnchor = new THREE.Object3D();
  private readonly input = {
    keys: new Set<string>(),
    fire: false,
    pulseQueued: false,
    touchActive: false,
    touchForward: false,
  };

  private skyMaterial: THREE.ShaderMaterial | null = null;
  private engineLeft!: THREE.Sprite;
  private engineRight!: THREE.Sprite;
  private toastTimer = 0;
  private toastMessage = '';
  private hasStarted = false;
  private isRunning = false;
  private missionComplete = false;
  private boostAudioTimer = 0;
  private collisionCooldown = 0;
  private contrailAccumulator = 0;
  private hudRefreshAccumulator = 0;

  private readonly player = {
    velocity: new THREE.Vector3(),
    yaw: 0,
    pitch: -0.03,
    bank: 0,
    fireCooldown: 0,
    pulseCooldown: 0,
    hitFlash: 0,
  };

  private readonly state = {
    elapsed: 0,
    score: 0,
    combo: 1,
    comboTime: 0,
    hull: 100,
    energy: 100,
    boost: 100,
    crystals: 0,
    drones: 0,
    rings: 0,
    threat: 0,
  };

  constructor(root: HTMLElement) {
    this.shell = requireElement<HTMLElement>(root, '.game-shell');
    this.viewport = requireElement<HTMLElement>(root, '#viewport');
    this.hud = this.collectHud(root);
    const radarContext = this.hud.radar.getContext('2d');
    if (!radarContext) {
      throw new Error('Radar canvas context is unavailable.');
    }
    this.radarContext = radarContext;

    this.textures = {
      terrain: createTerrainTexture('#4f7854', '#36554b', 'rgba(184,255,207,1)'),
      rock: createTerrainTexture('#796353', '#4d4038', 'rgba(255,215,158,0.9)'),
      glow: createGlowTexture('rgba(255,255,255,1)', 'rgba(126,247,255,0.48)'),
      ember: createGlowTexture('rgba(255,249,214,1)', 'rgba(255,146,82,0.55)'),
      cloud: createCloudTexture(),
    };

    this.textures.terrain.repeat.set(5, 5);
    this.textures.rock.repeat.set(4, 4);

    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.14;
    this.renderer.setClearColor(0x07131a);
    this.viewport.appendChild(this.renderer.domElement);

    this.scene.background = new THREE.Color(0x061117);
    this.scene.fog = new THREE.Fog(0x07131a, 180, 980);

    this.world.name = 'world';
    this.scene.add(this.world);

    this.composer = new EffectComposer(this.renderer);
    this.composer.addPass(new RenderPass(this.scene, this.camera));
    this.bloomPass = new UnrealBloomPass(new THREE.Vector2(1, 1), 0.85, 0.38, 0.35);
    this.composer.addPass(this.bloomPass);
    this.composer.addPass(new OutputPass());

    this.buildWorld();
    this.buildPlayer();
    this.resetRun();
    this.bindEvents();
    this.handleResize();
    this.refreshHud(true);
    this.drawRadar();

    this.clock.start();
    requestAnimationFrame(this.animate);
  }

  private collectHud(root: HTMLElement): HudRefs {
    return {
      startScreen: requireElement(root, '#start-screen'),
      gameOver: requireElement(root, '#game-over'),
      startButton: requireElement(root, '#start-button'),
      restartButton: requireElement(root, '#restart-button'),
      toast: requireElement(root, '#toast'),
      scoreValue: requireElement(root, '#score-value'),
      rankValue: requireElement(root, '#rank-value'),
      comboValue: requireElement(root, '#combo-value'),
      objectiveCrystal: requireElement(root, '#objective-crystal'),
      objectiveDrone: requireElement(root, '#objective-drone'),
      objectiveRing: requireElement(root, '#objective-ring'),
      missionStatus: requireElement(root, '#mission-status'),
      messageLine: requireElement(root, '#message-line'),
      sectorValue: requireElement(root, '#sector-value'),
      speedValue: requireElement(root, '#speed-value'),
      altitudeValue: requireElement(root, '#altitude-value'),
      threatValue: requireElement(root, '#threat-value'),
      hullMeter: requireElement(root, '#hull-meter'),
      energyMeter: requireElement(root, '#energy-meter'),
      boostMeter: requireElement(root, '#boost-meter'),
      crystalCount: requireElement(root, '#crystal-count'),
      droneCount: requireElement(root, '#drone-count'),
      ringCount: requireElement(root, '#ring-count'),
      radar: requireElement(root, '#radar'),
      finalScore: requireElement(root, '#final-score'),
      finalRank: requireElement(root, '#final-rank'),
      finalBrief: requireElement(root, '#final-brief'),
      finalStyle: requireElement(root, '#final-style'),
    };
  }

  private buildWorld(): void {
    const hemisphere = new THREE.HemisphereLight(0xa3e7ff, 0x1f160d, 1.5);
    const sun = new THREE.DirectionalLight(0xfff2c2, 1.25);
    sun.position.set(240, 280, -180);
    const rim = new THREE.DirectionalLight(0x7dd7ff, 0.6);
    rim.position.set(-160, 90, 120);

    this.world.add(hemisphere, sun, rim);

    this.createSky();
    this.createStars();
    this.createSunGlow();
    this.createClouds();
    this.createIslands();
    this.createCrystals();
    this.createRings();
    this.createDrones();
  }

  private createSky(): void {
    const material = new THREE.ShaderMaterial({
      side: THREE.BackSide,
      depthWrite: false,
      uniforms: {
        time: { value: 0 },
        topColor: { value: new THREE.Color(0x103a46) },
        bottomColor: { value: new THREE.Color(0x050a10) },
        sunColor: { value: new THREE.Color(0xffd78d) },
      },
      vertexShader: `
        varying vec3 vWorldPosition;
        void main() {
          vec4 worldPosition = modelMatrix * vec4(position, 1.0);
          vWorldPosition = worldPosition.xyz;
          gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
        }
      `,
      fragmentShader: `
        uniform float time;
        uniform vec3 topColor;
        uniform vec3 bottomColor;
        uniform vec3 sunColor;
        varying vec3 vWorldPosition;

        void main() {
          vec3 direction = normalize(vWorldPosition);
          float gradient = smoothstep(-0.2, 0.8, direction.y);
          vec3 base = mix(bottomColor, topColor, gradient);
          vec3 sunDirection = normalize(vec3(0.45, 0.82, -0.32));
          float sunGlow = pow(max(dot(direction, sunDirection), 0.0), 44.0);
          float horizon = pow(1.0 - abs(direction.y), 2.4);
          vec3 haze = vec3(0.06, 0.14, 0.18) * horizon * (0.8 + 0.2 * sin(time * 0.05));
          gl_FragColor = vec4(base + sunColor * sunGlow * 1.5 + haze, 1.0);
        }
      `,
    });

    this.skyMaterial = material;
    const sky = new THREE.Mesh(new THREE.SphereGeometry(1500, 40, 32), material);
    this.world.add(sky);
  }

  private createStars(): void {
    const geometry = new THREE.BufferGeometry();
    const positions: number[] = [];
    const colors: number[] = [];
    const colorA = new THREE.Color(0x8de5ff);
    const colorB = new THREE.Color(0xffe6a9);
    const mixed = new THREE.Color();

    for (let index = 0; index < 1200; index += 1) {
      const vector = new THREE.Vector3(
        THREE.MathUtils.randFloatSpread(2),
        THREE.MathUtils.randFloatSpread(2),
        THREE.MathUtils.randFloatSpread(2),
      )
        .normalize()
        .multiplyScalar(650 + Math.random() * 550);

      positions.push(vector.x, vector.y, vector.z);
      mixed.copy(index % 6 === 0 ? colorB : colorA).multiplyScalar(0.72 + Math.random() * 0.55);
      colors.push(mixed.r, mixed.g, mixed.b);
    }

    geometry.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3));
    geometry.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3));

    const points = new THREE.Points(
      geometry,
      new THREE.PointsMaterial({
        size: 3.2,
        vertexColors: true,
        transparent: true,
        opacity: 0.65,
        depthWrite: false,
        sizeAttenuation: true,
      }),
    );

    this.world.add(points);
  }

  private createSunGlow(): void {
    const material = new THREE.SpriteMaterial({
      map: this.textures.ember,
      color: 0xffd78d,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
    });
    const glow = new THREE.Sprite(material);
    glow.position.set(360, 460, -280);
    glow.scale.setScalar(180);
    this.world.add(glow);
  }

  private createClouds(): void {
    for (let index = 0; index < 28; index += 1) {
      const material = new THREE.SpriteMaterial({
        map: this.textures.cloud,
        color: index % 3 === 0 ? 0xffdfb8 : 0xcffcff,
        transparent: true,
        depthWrite: false,
        opacity: 0.18 + Math.random() * 0.08,
      });
      const sprite = new THREE.Sprite(material);
      const angle = (index / 28) * TAU;
      const radius = 170 + (index % 7) * 46 + Math.random() * 55;
      const position = new THREE.Vector3(
        Math.cos(angle) * radius,
        30 + Math.sin(index * 1.4) * 35 + Math.random() * 32,
        Math.sin(angle) * radius,
      );
      sprite.position.copy(position);
      const scale = 60 + Math.random() * 90;
      sprite.scale.set(scale * 1.3, scale, 1);
      this.world.add(sprite);
      this.clouds.push({
        sprite,
        base: position.clone(),
        drift: 0.05 + Math.random() * 0.1,
        bob: Math.random() * TAU,
      });
    }
  }

  private createIslands(): void {
    const specs = [
      { name: 'Citadel Rise', position: new THREE.Vector3(0, 6, 0), radius: 74, height: 102, seed: 1.2, tint: 0x78a86d },
      { name: 'Amber Shelf', position: new THREE.Vector3(182, 48, -146), radius: 54, height: 82, seed: 2.8, tint: 0x8ea66b },
      { name: 'Ghost Bastion', position: new THREE.Vector3(-216, 62, -152), radius: 58, height: 88, seed: 4.9, tint: 0x88b08d },
      { name: 'Glass Orchard', position: new THREE.Vector3(268, -24, 102), radius: 52, height: 78, seed: 7.1, tint: 0x6ca78c },
      { name: 'Sable Shelf', position: new THREE.Vector3(-154, -56, 196), radius: 60, height: 86, seed: 9.4, tint: 0x91a96e },
      { name: 'Aerial Forge', position: new THREE.Vector3(34, 116, 228), radius: 48, height: 74, seed: 12.6, tint: 0x79a173 },
      { name: 'Oracle Spine', position: new THREE.Vector3(-322, 18, 84), radius: 56, height: 92, seed: 15.2, tint: 0x8bb47f },
      { name: 'Mirage Perch', position: new THREE.Vector3(326, 92, -22), radius: 46, height: 72, seed: 18.5, tint: 0x8eb46b },
    ];

    for (const spec of specs) {
      this.islands.push(
        this.createIsland(
          spec.name,
          spec.position,
          spec.radius,
          spec.height,
          spec.seed,
          spec.tint,
        ),
      );
    }
  }

  private createIsland(
    name: string,
    position: THREE.Vector3,
    radius: number,
    height: number,
    seed: number,
    tint: THREE.ColorRepresentation,
  ): Island {
    const group = new THREE.Group();
    group.position.copy(position);

    const topGeometry = new THREE.CylinderGeometry(radius * 0.84, radius * 1.03, height * 0.28, 30, 5);
    this.distortIsland(topGeometry, seed, radius, false);
    const topMaterial = new THREE.MeshStandardMaterial({
      map: this.textures.terrain,
      color: tint,
      roughness: 0.96,
      metalness: 0.02,
    });
    const topMesh = new THREE.Mesh(topGeometry, topMaterial);
    topMesh.position.y = 6;
    group.add(topMesh);

    const undersideGeometry = new THREE.ConeGeometry(radius * 0.94, height, 30, 12);
    this.distortIsland(undersideGeometry, seed + 8.4, radius, true);
    const undersideMaterial = new THREE.MeshStandardMaterial({
      map: this.textures.rock,
      color: 0x776154,
      roughness: 0.94,
      metalness: 0.03,
    });
    const underside = new THREE.Mesh(undersideGeometry, undersideMaterial);
    underside.position.y = -height * 0.51;
    group.add(underside);

    for (let index = 0; index < 8; index += 1) {
      const angle = (index / 8) * TAU + seed;
      const shard = new THREE.Mesh(
        new THREE.DodecahedronGeometry(2.6 + Math.random() * 2.2, 0),
        new THREE.MeshStandardMaterial({
          color: index % 3 === 0 ? 0xa8ffd8 : 0x7cbde7,
          emissive: index % 3 === 0 ? 0x236a5d : 0x1b3f5c,
          emissiveIntensity: 1.7,
          roughness: 0.15,
          metalness: 0.75,
        }),
      );
      shard.position.set(
        Math.cos(angle) * radius * 0.42,
        10 + Math.random() * 4,
        Math.sin(angle) * radius * 0.42,
      );
      shard.rotation.set(Math.random() * TAU, Math.random() * TAU, Math.random() * TAU);
      group.add(shard);
    }

    const ring = new THREE.Mesh(
      new THREE.TorusGeometry(radius * 0.18, 1.4, 10, 22),
      new THREE.MeshBasicMaterial({
        color: 0xffd78d,
        transparent: true,
        opacity: 0.5,
      }),
    );
    ring.position.y = 11;
    ring.rotation.x = Math.PI / 2;
    group.add(ring);

    this.world.add(group);

    return {
      name,
      group,
      position: position.clone(),
      radius,
      colliderCenter: position.clone().add(new THREE.Vector3(0, -height * 0.18, 0)),
      colliderRadius: radius * 0.88,
    };
  }

  private distortIsland(
    geometry: THREE.BufferGeometry,
    seed: number,
    radius: number,
    underside: boolean,
  ): void {
    const positions = geometry.attributes.position as THREE.BufferAttribute;

    for (let index = 0; index < positions.count; index += 1) {
      const x = positions.getX(index);
      const y = positions.getY(index);
      const z = positions.getZ(index);
      const radial = Math.sqrt(x * x + z * z);
      const noise = fbm(x * 0.07 + seed, z * 0.07 - seed, seed);
      const scale = 1 + (noise - 0.5) * (underside ? 0.18 : 0.1);
      let nextY = y;

      if (underside) {
        nextY -= noise * 8 + (radial / radius) * 6;
      } else {
        nextY += noise * 6 - (radial / radius) * 1.5;
      }

      positions.setXYZ(index, x * scale, nextY, z * scale);
    }

    positions.needsUpdate = true;
    geometry.computeVertexNormals();
  }

  private createCrystals(): void {
    for (const [index, island] of this.islands.entries()) {
      for (let offset = 0; offset < 2; offset += 1) {
        const angle = (index * 1.4 + offset * Math.PI + 0.4) % TAU;
        const position = island.position
          .clone()
          .add(
            new THREE.Vector3(
              Math.cos(angle) * island.radius * 0.55,
              14 + offset * 3,
              Math.sin(angle) * island.radius * 0.55,
            ),
          );
        this.crystals.push(this.createCrystal(position));
      }
    }
  }

  private createCrystal(position: THREE.Vector3): Crystal {
    const group = new THREE.Group();
    group.position.copy(position);

    const core = new THREE.Mesh(
      new THREE.OctahedronGeometry(2.8, 0),
      new THREE.MeshStandardMaterial({
        color: 0xc8fff0,
        emissive: 0x3dbda2,
        emissiveIntensity: 2.2,
        roughness: 0.08,
        metalness: 0.78,
      }),
    );
    group.add(core);

    const halo = new THREE.Sprite(
      new THREE.SpriteMaterial({
        map: this.textures.glow,
        color: 0x84fff0,
        transparent: true,
        depthWrite: false,
        blending: THREE.AdditiveBlending,
      }),
    );
    halo.scale.setScalar(9.6);
    group.add(halo);

    const baseRing = new THREE.Mesh(
      new THREE.TorusGeometry(4.4, 0.35, 8, 26),
      new THREE.MeshBasicMaterial({
        color: 0xffd78d,
        transparent: true,
        opacity: 0.55,
      }),
    );
    baseRing.rotation.x = Math.PI / 2;
    baseRing.position.y = -2.6;
    group.add(baseRing);

    this.world.add(group);
    return {
      group,
      basePosition: position.clone(),
      bobOffset: Math.random() * TAU,
      active: true,
      cooldown: 0,
    };
  }

  private createRings(): void {
    const addLane = (
      fromIndex: number,
      toIndex: number,
      laneName: string,
      heightOffset: number,
      sway: number,
    ): void => {
      const from = this.islands[fromIndex].position;
      const to = this.islands[toIndex].position;
      const middle = from.clone().lerp(to, 0.5);
      const direction = to.clone().sub(from).normalize();
      const side = new THREE.Vector3(-direction.z, 0, direction.x).multiplyScalar(sway);
      const position = middle.add(side).add(new THREE.Vector3(0, heightOffset, 0));
      this.rings.push(this.createRing(position, direction, laneName));
    };

    addLane(0, 1, 'Amber Run', 22, 16);
    addLane(0, 2, 'Ghost Cut', 18, -18);
    addLane(0, 3, 'Glass Dive', -4, 20);
    addLane(0, 4, 'Sable Drift', -8, -22);
    addLane(0, 5, 'Zenith Climb', 30, 12);
    addLane(1, 7, 'Mirage Slip', 24, 18);
    addLane(2, 6, 'Oracle Vein', 18, -14);
  }

  private createRing(position: THREE.Vector3, direction: THREE.Vector3, laneName: string): Ring {
    const group = new THREE.Group();
    group.position.copy(position);
    group.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction.clone().normalize());

    const haloMaterial = new THREE.MeshBasicMaterial({
      color: 0x8af0ff,
      transparent: true,
      opacity: 0.78,
    });
    const halo = new THREE.Mesh(new THREE.TorusGeometry(14, 0.72, 14, 48), haloMaterial);
    group.add(halo);

    const outer = new THREE.Mesh(
      new THREE.TorusGeometry(16.8, 0.42, 8, 36),
      new THREE.MeshBasicMaterial({
        color: 0xffc857,
        transparent: true,
        opacity: 0.36,
      }),
    );
    group.add(outer);

    for (let index = 0; index < 4; index += 1) {
      const marker = new THREE.Mesh(
        new THREE.BoxGeometry(1.5, 0.6, 4.4),
        new THREE.MeshBasicMaterial({ color: 0xfff4d1 }),
      );
      const angle = (index / 4) * TAU;
      marker.position.set(Math.cos(angle) * 14, 0, Math.sin(angle) * 14);
      marker.lookAt(new THREE.Vector3(0, 0, 0));
      group.add(marker);
    }

    this.world.add(group);
    return {
      group,
      halo,
      radius: 14,
      cooldown: 0,
      active: true,
      pulse: Math.random() * TAU,
      laneName,
    };
  }

  private createDrones(): void {
    for (const [index, island] of this.islands.entries()) {
      if (index === 0) {
        continue;
      }

      const angle = index * 0.78;
      const anchor = island.position
        .clone()
        .add(new THREE.Vector3(Math.cos(angle) * island.radius * 0.72, 24, Math.sin(angle) * island.radius * 0.72));
      this.drones.push(this.createDrone(anchor, index * 0.9));
    }
  }

  private createDrone(anchor: THREE.Vector3, phase: number): Drone {
    const group = new THREE.Group();
    group.position.copy(anchor);

    const bodyMaterial = new THREE.MeshStandardMaterial({
      color: 0x2a3945,
      emissive: 0x113a45,
      emissiveIntensity: 1.2,
      roughness: 0.3,
      metalness: 0.88,
    });

    const core = new THREE.Mesh(new THREE.OctahedronGeometry(2.6, 0), bodyMaterial);
    core.scale.set(1.1, 0.65, 1.1);
    group.add(core);

    const ring = new THREE.Mesh(
      new THREE.TorusGeometry(3.5, 0.35, 10, 18),
      new THREE.MeshStandardMaterial({
        color: 0x71d3ff,
        emissive: 0x1f7cb2,
        emissiveIntensity: 1.8,
        roughness: 0.18,
        metalness: 0.82,
      }),
    );
    ring.rotation.x = Math.PI / 2;
    group.add(ring);

    for (let index = 0; index < 4; index += 1) {
      const arm = new THREE.Mesh(
        new THREE.BoxGeometry(0.45, 0.45, 3.2),
        new THREE.MeshStandardMaterial({
          color: 0xa4d8ff,
          roughness: 0.24,
          metalness: 0.72,
        }),
      );
      arm.position.set(
        index < 2 ? -2.7 : 2.7,
        0,
        index % 2 === 0 ? -1.2 : 1.2,
      );
      arm.rotation.y = index < 2 ? 0.2 : -0.2;
      group.add(arm);
    }

    const eye = new THREE.Sprite(
      new THREE.SpriteMaterial({
        map: this.textures.ember,
        color: 0xffaa73,
        transparent: true,
        depthWrite: false,
        blending: THREE.AdditiveBlending,
      }),
    );
    eye.scale.setScalar(5.8);
    eye.position.set(0, 0, -3.4);
    group.add(eye);

    this.world.add(group);
    return {
      group,
      eye,
      velocity: new THREE.Vector3(),
      anchor: anchor.clone(),
      phase,
      alive: true,
      health: 3,
      cooldown: 1.2 + Math.random(),
      respawn: 0,
    };
  }

  private buildPlayer(): void {
    const hullMaterial = new THREE.MeshStandardMaterial({
      color: 0xbbefff,
      emissive: 0x123c4b,
      emissiveIntensity: 0.9,
      roughness: 0.22,
      metalness: 0.78,
    });
    const accentMaterial = new THREE.MeshStandardMaterial({
      color: 0xffc857,
      emissive: 0x7d4c08,
      emissiveIntensity: 1.2,
      roughness: 0.3,
      metalness: 0.74,
    });
    const glassMaterial = new THREE.MeshPhysicalMaterial({
      color: 0x8deaff,
      emissive: 0x286a82,
      emissiveIntensity: 1.1,
      transparent: true,
      opacity: 0.62,
      roughness: 0.05,
      metalness: 0.05,
      clearcoat: 1,
      clearcoatRoughness: 0.12,
    });

    const fuselage = new THREE.Mesh(new THREE.CylinderGeometry(1.05, 1.42, 10, 12), hullMaterial);
    fuselage.rotation.x = Math.PI / 2;
    this.ship.add(fuselage);

    const nose = new THREE.Mesh(new THREE.ConeGeometry(1.08, 4, 10), accentMaterial);
    nose.rotation.x = Math.PI / 2;
    nose.position.z = -6.8;
    this.ship.add(nose);

    const rear = new THREE.Mesh(new THREE.ConeGeometry(1.24, 2.4, 10), hullMaterial);
    rear.rotation.x = -Math.PI / 2;
    rear.position.z = 6.1;
    this.ship.add(rear);

    const wings = new THREE.Mesh(new THREE.BoxGeometry(7.8, 0.28, 2.2), hullMaterial);
    wings.position.set(0, -0.25, -0.4);
    this.ship.add(wings);

    const finLeft = new THREE.Mesh(new THREE.BoxGeometry(0.22, 1.8, 3.5), accentMaterial);
    finLeft.position.set(-2.9, 0.74, 0.5);
    finLeft.rotation.y = 0.2;
    this.ship.add(finLeft);

    const finRight = finLeft.clone();
    finRight.position.x = 2.9;
    finRight.rotation.y = -0.2;
    this.ship.add(finRight);

    const cockpit = new THREE.Mesh(new THREE.SphereGeometry(1.32, 18, 14), glassMaterial);
    cockpit.scale.set(0.88, 0.55, 1.2);
    cockpit.position.set(0, 0.82, -0.7);
    this.ship.add(cockpit);

    const noseRing = new THREE.Mesh(
      new THREE.TorusGeometry(1.6, 0.18, 8, 20),
      new THREE.MeshBasicMaterial({ color: 0xffe7ae, transparent: true, opacity: 0.72 }),
    );
    noseRing.rotation.x = Math.PI / 2;
    noseRing.position.z = -5.2;
    this.ship.add(noseRing);

    this.noseAnchor.position.set(0, 0.16, -8.4);
    this.ship.add(this.noseAnchor);

    const engineMaterialLeft = new THREE.SpriteMaterial({
      map: this.textures.glow,
      color: 0x7ef7ff,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
    });
    this.engineLeft = new THREE.Sprite(engineMaterialLeft);
    this.engineLeft.position.set(-1.62, -0.06, 4.2);
    this.engineLeft.scale.setScalar(4.3);
    this.ship.add(this.engineLeft);

    const engineMaterialRight = engineMaterialLeft.clone();
    this.engineRight = new THREE.Sprite(engineMaterialRight);
    this.engineRight.position.set(1.62, -0.06, 4.2);
    this.engineRight.scale.setScalar(4.3);
    this.ship.add(this.engineRight);

    const underGlow = new THREE.PointLight(0x7ef7ff, 0.9, 44, 2);
    underGlow.position.set(0, -0.4, 1.4);
    this.ship.add(underGlow);

    this.playerGroup.add(this.ship);
    this.world.add(this.playerGroup);
  }

  private bindEvents(): void {
    this.hud.startButton.addEventListener('click', () => {
      void this.startRun();
    });
    this.hud.restartButton.addEventListener('click', () => {
      void this.startRun();
    });

    window.addEventListener('resize', this.handleResize);
    window.addEventListener('blur', this.resetInputState);

    window.addEventListener('keydown', (event) => {
      this.input.keys.add(event.code);
      if (event.code === 'KeyF') {
        this.input.pulseQueued = true;
      }
      if (event.code === 'Enter' && !this.isRunning) {
        void this.startRun();
      }
    });

    window.addEventListener('keyup', (event) => {
      this.input.keys.delete(event.code);
    });

    this.viewport.addEventListener('mousemove', (event) => {
      const rect = this.viewport.getBoundingClientRect();
      const x = (event.clientX - rect.left) / rect.width;
      const y = (event.clientY - rect.top) / rect.height;
      this.pointer.set(THREE.MathUtils.clamp(x * 2 - 1, -1, 1), THREE.MathUtils.clamp(-(y * 2 - 1), -1, 1));
    });

    this.viewport.addEventListener('mouseleave', () => {
      this.pointer.multiplyScalar(0.7);
    });

    this.viewport.addEventListener('mousedown', (event) => {
      if (event.button === 0) {
        this.input.fire = true;
      }
      if (event.button === 2) {
        this.input.pulseQueued = true;
      }
    });

    window.addEventListener('mouseup', (event) => {
      if (event.button === 0) {
        this.input.fire = false;
      }
    });

    this.viewport.addEventListener('contextmenu', (event) => {
      event.preventDefault();
    });

    this.viewport.addEventListener(
      'touchstart',
      (event) => {
        event.preventDefault();
        this.input.touchActive = true;
        this.input.touchForward = true;
        this.input.fire = event.touches.length > 1;
        const touch = event.touches[0];
        const rect = this.viewport.getBoundingClientRect();
        const x = (touch.clientX - rect.left) / rect.width;
        const y = (touch.clientY - rect.top) / rect.height;
        this.pointer.set(THREE.MathUtils.clamp(x * 2 - 1, -1, 1), THREE.MathUtils.clamp(-(y * 2 - 1), -1, 1));
      },
      { passive: false },
    );

    this.viewport.addEventListener(
      'touchmove',
      (event) => {
        event.preventDefault();
        const touch = event.touches[0];
        if (!touch) {
          return;
        }
        this.input.fire = event.touches.length > 1;
        const rect = this.viewport.getBoundingClientRect();
        const x = (touch.clientX - rect.left) / rect.width;
        const y = (touch.clientY - rect.top) / rect.height;
        this.pointer.set(THREE.MathUtils.clamp(x * 2 - 1, -1, 1), THREE.MathUtils.clamp(-(y * 2 - 1), -1, 1));
      },
      { passive: false },
    );

    this.viewport.addEventListener(
      'touchend',
      (event) => {
        event.preventDefault();
        this.input.fire = false;
        if (event.touches.length === 0) {
          this.input.touchActive = false;
          this.input.touchForward = false;
          this.pointer.multiplyScalar(0.35);
        }
      },
      { passive: false },
    );
  }

  private readonly handleResize = (): void => {
    const width = Math.max(1, this.viewport.clientWidth);
    const height = Math.max(1, this.viewport.clientHeight);
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.setSize(width, height, false);
    this.composer.setSize(width, height);

    const radarRect = this.hud.radar.getBoundingClientRect();
    const dpr = Math.min(window.devicePixelRatio, 2);
    this.hud.radar.width = Math.max(180, Math.round(radarRect.width * dpr));
    this.hud.radar.height = Math.max(180, Math.round(radarRect.height * dpr));
  };

  private readonly resetInputState = (): void => {
    this.input.keys.clear();
    this.input.fire = false;
    this.input.pulseQueued = false;
    this.input.touchActive = false;
    this.input.touchForward = false;
  };

  private async startRun(): Promise<void> {
    await this.audio.start();
    this.hasStarted = true;
    this.isRunning = true;
    this.hud.startScreen.classList.remove('overlay--visible');
    this.hud.gameOver.classList.remove('overlay--visible');
    this.shell.classList.add('is-live');
    this.resetRun();
    this.showToast('Run hot. The frontier is yours.', 2.8);
  }

  private resetRun(): void {
    this.state.elapsed = 0;
    this.state.score = 0;
    this.state.combo = 1;
    this.state.comboTime = 0;
    this.state.hull = 100;
    this.state.energy = 100;
    this.state.boost = 100;
    this.state.crystals = 0;
    this.state.drones = 0;
    this.state.rings = 0;
    this.state.threat = 0;
    this.missionComplete = false;
    this.toastTimer = 0;
    this.toastMessage = 'Click launch and take the sky.';

    this.player.velocity.set(0, 0, 0);
    this.player.yaw = 0;
    this.player.pitch = -0.03;
    this.player.bank = 0;
    this.player.fireCooldown = 0;
    this.player.pulseCooldown = 0;
    this.player.hitFlash = 0;
    this.playerGroup.position.set(0, 26, 148);
    this.playerGroup.quaternion.identity();
    this.ship.rotation.set(0, 0, 0);

    for (const crystal of this.crystals) {
      crystal.active = true;
      crystal.cooldown = 0;
      crystal.group.visible = true;
      crystal.group.position.copy(crystal.basePosition);
    }

    for (const ring of this.rings) {
      ring.active = true;
      ring.cooldown = 0;
      ring.group.visible = true;
      (ring.halo.material as THREE.MeshBasicMaterial).opacity = 0.78;
    }

    for (const drone of this.drones) {
      drone.alive = true;
      drone.health = 3;
      drone.cooldown = 0.8 + Math.random();
      drone.respawn = 0;
      drone.velocity.set(0, 0, 0);
      drone.group.position.copy(drone.anchor);
      drone.group.visible = true;
    }

    this.clearProjectiles();
    this.clearParticles();
    this.refreshHud(true);
    this.drawRadar();
  }

  private clearProjectiles(): void {
    for (const projectile of this.projectiles) {
      this.world.remove(projectile.sprite);
      (projectile.sprite.material as THREE.SpriteMaterial).dispose();
    }
    this.projectiles.length = 0;
  }

  private clearParticles(): void {
    for (const particle of this.particles) {
      this.world.remove(particle.mesh);
      (particle.mesh.material as THREE.MeshBasicMaterial).dispose();
    }
    this.particles.length = 0;
  }

  private readonly animate = (): void => {
    const delta = Math.min(this.clock.getDelta(), 0.033);
    this.state.elapsed += delta;

    if (this.skyMaterial) {
      this.skyMaterial.uniforms.time.value = this.state.elapsed;
    }

    this.updateToast(delta);
    this.updateWorldAnimation(delta);

    if (this.isRunning) {
      this.updateRun(delta);
    } else {
      this.updateAttractMode(delta);
    }

    this.hudRefreshAccumulator += delta;
    if (this.hudRefreshAccumulator >= 1 / 30) {
      this.hudRefreshAccumulator = 0;
      this.refreshHud(false);
      this.drawRadar();
    }

    this.composer.render();
    requestAnimationFrame(this.animate);
  };

  private updateAttractMode(delta: number): void {
    const orbit = this.state.elapsed * 0.22;
    this.camera.position.set(Math.sin(orbit) * 160, 72 + Math.sin(orbit * 0.7) * 18, Math.cos(orbit) * 160);
    this.camera.lookAt(0, 18, 0);
    this.pointer.lerp(new THREE.Vector2(0, 0), delta * 2);
    this.player.pitch = THREE.MathUtils.damp(this.player.pitch, 0.02, 2.5, delta);
    this.ship.rotation.z = THREE.MathUtils.damp(this.ship.rotation.z, 0.18, 2.5, delta);
  }

  private updateRun(delta: number): void {
    this.player.fireCooldown = Math.max(0, this.player.fireCooldown - delta);
    this.player.pulseCooldown = Math.max(0, this.player.pulseCooldown - delta);
    this.player.hitFlash = Math.max(0, this.player.hitFlash - delta * 1.9);
    this.boostAudioTimer = Math.max(0, this.boostAudioTimer - delta);
    this.collisionCooldown = Math.max(0, this.collisionCooldown - delta);

    if (this.state.comboTime > 0) {
      this.state.comboTime = Math.max(0, this.state.comboTime - delta);
    } else {
      this.state.combo = THREE.MathUtils.damp(this.state.combo, 1, 2.5, delta);
    }

    this.updatePlayer(delta);
    this.updateCrystals(delta);
    this.updateRings(delta);
    this.updateDrones(delta);
    this.updateProjectiles(delta);
    this.updateParticles(delta);
    this.updateThreat();
    this.checkMissionCompletion();

    if (this.state.hull <= 0) {
      this.finishRun();
    }
  }

  private updateWorldAnimation(delta: number): void {
    for (const cloud of this.clouds) {
      const lift = Math.sin(this.state.elapsed * cloud.drift + cloud.bob) * 6;
      const drift = Math.cos(this.state.elapsed * cloud.drift * 0.6 + cloud.bob) * 9;
      cloud.sprite.position.set(cloud.base.x + drift, cloud.base.y + lift, cloud.base.z);
      cloud.sprite.material.rotation += delta * 0.015;
    }

    for (const crystal of this.crystals) {
      crystal.group.rotation.y += delta * 1.7;
      crystal.group.rotation.x = Math.sin(this.state.elapsed * 1.2 + crystal.bobOffset) * 0.18;
    }

    for (const ring of this.rings) {
      ring.pulse += delta * 2.2;
      ring.group.rotation.y += delta * 0.3;
      const baseOpacity = ring.active ? 0.62 + Math.sin(ring.pulse) * 0.1 : 0.16;
      (ring.halo.material as THREE.MeshBasicMaterial).opacity = baseOpacity;
      const scale = ring.active ? 1 + Math.sin(ring.pulse * 2) * 0.02 : 0.94;
      ring.group.scale.setScalar(scale);
    }
  }

  private updatePlayer(delta: number): void {
    const forwardIntent = THREE.MathUtils.clamp(
      axisFromKeys(this.input.keys, 'KeyW', 'KeyS') + (this.input.touchForward ? 1 : 0),
      -1,
      1,
    );
    const strafeIntent = axisFromKeys(this.input.keys, 'KeyD', 'KeyA');
    const verticalIntent = axisFromKeys(this.input.keys, 'Space', 'KeyC');
    const boostActive =
      (this.input.keys.has('ShiftLeft') || this.input.keys.has('ShiftRight')) &&
      this.state.boost > 0 &&
      forwardIntent > 0;

    const pointerYaw = this.pointer.x * 1.6;
    const pointerPitch = this.pointer.y * 0.52;
    this.player.yaw -= pointerYaw * delta * 1.7;
    this.player.pitch = THREE.MathUtils.damp(
      this.player.pitch,
      pointerPitch - verticalIntent * 0.08,
      5.2,
      delta,
    );
    this.player.bank = THREE.MathUtils.damp(
      this.player.bank,
      -strafeIntent * 0.42 - this.pointer.x * 0.32,
      6.4,
      delta,
    );

    this.playerGroup.quaternion.setFromEuler(
      new THREE.Euler(this.player.pitch, this.player.yaw, 0, 'YXZ'),
    );
    this.ship.rotation.z = this.player.bank;

    const forward = this.tempA.set(0, 0, -1).applyQuaternion(this.playerGroup.quaternion).normalize();
    const right = this.tempB.set(1, 0, 0).applyQuaternion(this.playerGroup.quaternion).normalize();
    const acceleration = this.tempC.set(0, 0, 0);
    acceleration.addScaledVector(forward, forwardIntent * 92);
    acceleration.addScaledVector(right, strafeIntent * 60);
    acceleration.addScaledVector(new THREE.Vector3(0, 1, 0), verticalIntent * 56);

    if (boostActive) {
      acceleration.addScaledVector(forward, 84);
      this.state.boost = Math.max(0, this.state.boost - 26 * delta);
      if (this.boostAudioTimer === 0) {
        this.audio.boost();
        this.boostAudioTimer = 0.28;
      }
    } else {
      this.state.boost = Math.min(100, this.state.boost + 18 * delta);
    }

    this.state.energy = Math.min(100, this.state.energy + 12 * delta);

    if (this.input.fire && this.player.fireCooldown === 0 && this.state.energy >= 1.4) {
      this.spawnPlayerProjectile();
      this.player.fireCooldown = 0.12;
      this.state.energy = Math.max(0, this.state.energy - 1.4);
      this.audio.shoot();
    }

    if (this.input.pulseQueued) {
      this.input.pulseQueued = false;
      if (this.player.pulseCooldown === 0 && this.state.energy >= 24) {
        this.triggerPulse();
      } else {
        this.showToast('Pulse not ready.', 1.1);
      }
    }

    this.player.velocity.addScaledVector(acceleration, delta);
    this.player.velocity.addScaledVector(this.player.velocity, -(boostActive ? 0.85 : 1.35) * delta);

    const maxSpeed = boostActive ? PLAYER_BOOST_SPEED : PLAYER_CRUISE_SPEED;
    const speed = this.player.velocity.length();
    if (speed > maxSpeed) {
      this.player.velocity.multiplyScalar(maxSpeed / speed);
    }

    this.playerGroup.position.addScaledVector(this.player.velocity, delta);
    this.applyWorldBounds();
    this.resolveIslandCollisions();
    this.updateShipVisuals(speed, boostActive, delta);
    this.updateCamera(delta, forward);
  }

  private updateCamera(delta: number, forward: THREE.Vector3): void {
    const right = this.tempB.set(1, 0, 0).applyQuaternion(this.playerGroup.quaternion);
    const desiredPosition = this.playerGroup.position
      .clone()
      .addScaledVector(forward, -19.5)
      .addScaledVector(right, this.pointer.x * 2.8)
      .add(new THREE.Vector3(0, 6.2, 0));
    this.camera.position.lerp(desiredPosition, 1 - Math.exp(-delta * 4.6));

    this.lookTarget
      .copy(this.playerGroup.position)
      .addScaledVector(forward, 32)
      .add(new THREE.Vector3(0, 1.8 + this.pointer.y * 2.5, 0));
    this.camera.lookAt(this.lookTarget);

    const speed = this.player.velocity.length();
    this.camera.fov = THREE.MathUtils.damp(this.camera.fov, 62 + clamp01((speed - 60) / 70) * 8, 4, delta);
    this.camera.updateProjectionMatrix();
  }

  private updateShipVisuals(speed: number, boostActive: boolean, delta: number): void {
    const engineScale = 4.2 + clamp01(speed / PLAYER_BOOST_SPEED) * 2.4 + (boostActive ? 1.7 : 0);
    this.engineLeft.scale.setScalar(engineScale + Math.sin(this.state.elapsed * 24) * 0.18);
    this.engineRight.scale.setScalar(engineScale + Math.sin(this.state.elapsed * 24 + 1.1) * 0.18);
    const engineOpacity = 0.52 + clamp01(speed / PLAYER_BOOST_SPEED) * 0.35 + (boostActive ? 0.14 : 0);
    (this.engineLeft.material as THREE.SpriteMaterial).opacity = engineOpacity;
    (this.engineRight.material as THREE.SpriteMaterial).opacity = engineOpacity;

    const flash = this.player.hitFlash;
    for (const child of this.ship.children) {
      if (child instanceof THREE.Mesh && child.material instanceof THREE.MeshStandardMaterial) {
        child.material.emissiveIntensity = flash > 0 ? 1.7 + flash * 2.2 : 1;
      }
    }

    this.contrailAccumulator += delta * (boostActive ? 1.9 : 1.05) * clamp01(speed / 40);
    if (this.contrailAccumulator >= 0.08) {
      this.contrailAccumulator = 0;
      this.spawnContrail();
    }
  }

  private applyWorldBounds(): void {
    const distance = this.playerGroup.position.length();
    if (distance <= WORLD_LIMIT) {
      return;
    }

    const normal = this.playerGroup.position.clone().normalize();
    this.playerGroup.position.copy(normal.multiplyScalar(WORLD_LIMIT));
    const outwardVelocity = this.player.velocity.dot(normal);
    if (outwardVelocity > 0) {
      this.player.velocity.addScaledVector(normal, -outwardVelocity * 1.6);
    }
    this.showToast('Frontier edge reached. Turning you back in.', 1.4);
  }

  private resolveIslandCollisions(): void {
    for (const island of this.islands) {
      const offset = this.playerGroup.position.clone().sub(island.colliderCenter);
      const distance = offset.length();
      const minimum = island.colliderRadius + PLAYER_RADIUS;
      if (distance >= minimum) {
        continue;
      }

      offset.normalize();
      this.playerGroup.position.copy(island.colliderCenter).addScaledVector(offset, minimum + 0.2);
      const collisionVelocity = this.player.velocity.dot(offset);
      if (collisionVelocity < 0) {
        this.player.velocity.addScaledVector(offset, -collisionVelocity * 1.6);
      }

      if (this.collisionCooldown === 0) {
        this.damagePlayer(10, 'Island impact.');
        this.collisionCooldown = 0.4;
      }
    }
  }

  private updateCrystals(delta: number): void {
    for (const crystal of this.crystals) {
      if (crystal.active) {
        crystal.group.position.y =
          crystal.basePosition.y + Math.sin(this.state.elapsed * 1.9 + crystal.bobOffset) * 1.7;
        if (crystal.group.position.distanceTo(this.playerGroup.position) < 8.5) {
          crystal.active = false;
          crystal.cooldown = 12 + Math.random() * 8;
          crystal.group.visible = false;
          this.state.crystals += 1;
          this.state.energy = Math.min(100, this.state.energy + 18);
          this.state.boost = Math.min(100, this.state.boost + 20);
          this.awardScore(75);
          this.spawnBurst(crystal.basePosition, 0x8ffff0, 14, 24, 0.32);
          this.audio.pickup();
          this.showToast('Crystal banked. Energy surged.', 1.3);
        }
      } else {
        crystal.cooldown = Math.max(0, crystal.cooldown - delta);
        if (crystal.cooldown === 0) {
          crystal.active = true;
          crystal.group.visible = true;
          this.spawnBurst(crystal.basePosition, 0x6de8ff, 10, 18, 0.24);
        }
      }
    }
  }

  private updateRings(delta: number): void {
    for (const ring of this.rings) {
      if (ring.active) {
        const localPlayer = this.playerGroup.position
          .clone()
          .sub(ring.group.position)
          .applyQuaternion(this.tempQuaternion.copy(ring.group.quaternion).invert());
        const radial = Math.hypot(localPlayer.x, localPlayer.z);
        if (Math.abs(localPlayer.y) < 3.2 && radial < ring.radius * 0.72) {
          ring.active = false;
          ring.cooldown = 11 + Math.random() * 5;
          (ring.halo.material as THREE.MeshBasicMaterial).opacity = 0.12;
          this.state.rings += 1;
          this.state.boost = Math.min(100, this.state.boost + 28);
          this.awardScore(120);
          this.spawnBurst(ring.group.position, 0xffd78d, 16, 28, 0.34);
          this.audio.ring();
          this.showToast(`${ring.laneName} threaded clean.`, 1.5);
        }
      } else {
        ring.cooldown = Math.max(0, ring.cooldown - delta);
        if (ring.cooldown === 0) {
          ring.active = true;
          this.spawnBurst(ring.group.position, 0x8af0ff, 10, 18, 0.22);
        }
      }
    }
  }

  private updateDrones(delta: number): void {
    for (const drone of this.drones) {
      if (!drone.alive) {
        drone.respawn = Math.max(0, drone.respawn - delta);
        if (drone.respawn === 0) {
          drone.alive = true;
          drone.health = 3;
          drone.group.visible = true;
          drone.group.position.copy(drone.anchor);
          drone.velocity.set(0, 0, 0);
          this.spawnBurst(drone.anchor, 0xffb672, 10, 18, 0.2);
        }
        continue;
      }

      const targetOffset = new THREE.Vector3(
        Math.cos(this.state.elapsed * 0.58 + drone.phase) * 18,
        6 + Math.sin(this.state.elapsed * 1.3 + drone.phase) * 6,
        Math.sin(this.state.elapsed * 0.58 + drone.phase) * 18,
      );
      const patrolTarget = drone.anchor.clone().add(targetOffset);
      const toPlayer = this.playerGroup.position.clone().sub(drone.group.position);
      const playerDistance = toPlayer.length();
      const chaseTarget = this.playerGroup.position.clone().add(
        new THREE.Vector3(
          Math.sin(this.state.elapsed + drone.phase) * 8,
          3,
          Math.cos(this.state.elapsed * 1.1 + drone.phase) * 8,
        ),
      );
      const desired = patrolTarget.lerp(chaseTarget, clamp01((180 - playerDistance) / 140) * 0.7);
      const desiredVelocity = desired.sub(drone.group.position).multiplyScalar(0.95);
      drone.velocity.lerp(desiredVelocity, delta * 1.7);

      const speedLimit = playerDistance < 170 ? 60 : 34;
      if (drone.velocity.length() > speedLimit) {
        drone.velocity.setLength(speedLimit);
      }

      drone.group.position.addScaledVector(drone.velocity, delta);
      drone.group.lookAt(playerDistance < 210 ? this.playerGroup.position : patrolTarget);
      drone.eye.scale.setScalar(5.6 + Math.sin(this.state.elapsed * 7 + drone.phase) * 0.6);
      drone.cooldown = Math.max(0, drone.cooldown - delta);

      if (playerDistance < 148 && drone.cooldown === 0) {
        this.spawnDroneProjectile(drone);
        drone.cooldown = 1.15 + Math.random() * 0.45;
      }

      if (playerDistance < PLAYER_RADIUS + 5.2 && this.collisionCooldown === 0) {
        this.damagePlayer(8, 'Drone clipped your hull.');
        this.collisionCooldown = 0.45;
      }
    }
  }

  private updateProjectiles(delta: number): void {
    for (let index = this.projectiles.length - 1; index >= 0; index -= 1) {
      const projectile = this.projectiles[index];
      projectile.life -= delta;
      projectile.sprite.position.addScaledVector(projectile.velocity, delta);

      if (projectile.life <= 0 || projectile.sprite.position.length() > WORLD_LIMIT + 300) {
        this.removeProjectile(index);
        continue;
      }

      if (projectile.friendly) {
        let hitDrone = false;
        for (const drone of this.drones) {
          if (!drone.alive) {
            continue;
          }
          if (drone.group.position.distanceTo(projectile.sprite.position) < projectile.radius + 4.8) {
            drone.health -= projectile.damage;
            this.spawnBurst(projectile.sprite.position, 0x8af0ff, 6, 14, 0.18);
            this.removeProjectile(index);
            if (drone.health <= 0) {
              this.destroyDrone(drone);
            }
            hitDrone = true;
            break;
          }
        }
        if (hitDrone) {
          continue;
        }
      } else if (projectile.sprite.position.distanceTo(this.playerGroup.position) < projectile.radius + PLAYER_RADIUS) {
        this.spawnBurst(projectile.sprite.position, 0xff9b6b, 10, 16, 0.22);
        this.removeProjectile(index);
        this.damagePlayer(7, 'Incoming fire connected.');
      }
    }
  }

  private updateParticles(delta: number): void {
    for (let index = this.particles.length - 1; index >= 0; index -= 1) {
      const particle = this.particles[index];
      particle.life -= delta;
      if (particle.life <= 0) {
        this.removeParticle(index);
        continue;
      }
      particle.mesh.position.addScaledVector(particle.velocity, delta);
      particle.velocity.multiplyScalar(1 - delta * 1.3);
      const alpha = particle.life / particle.maxLife;
      (particle.mesh.material as THREE.MeshBasicMaterial).opacity = alpha;
      particle.mesh.scale.addScalar(particle.growth * delta);
    }
  }

  private updateThreat(): void {
    const aliveDrones = this.drones.filter((drone) => drone.alive).length;
    const hullRisk = 1 - this.state.hull / 100;
    this.state.threat = THREE.MathUtils.clamp(
      aliveDrones * 8 + hullRisk * 42 + (this.missionComplete ? 12 : 20),
      0,
      100,
    );
  }

  private checkMissionCompletion(): void {
    if (
      !this.missionComplete &&
      this.state.crystals >= CRYSTAL_TARGET &&
      this.state.drones >= DRONE_TARGET &&
      this.state.rings >= RING_TARGET
    ) {
      this.missionComplete = true;
      this.awardScore(1000);
      this.showToast('Sector stabilized. Free roam unlocked.', 3.4);
    }
  }

  private spawnPlayerProjectile(): void {
    const origin = this.noseAnchor.getWorldPosition(new THREE.Vector3());
    const direction = new THREE.Vector3(0, 0, -1)
      .applyQuaternion(this.playerGroup.quaternion)
      .normalize();
    this.spawnProjectile(origin, direction, true, 220, 0x8ffff0, 0.5, 0.5);
  }

  private spawnDroneProjectile(drone: Drone): void {
    const origin = drone.group.position
      .clone()
      .add(new THREE.Vector3(0, 0, -3.2).applyQuaternion(drone.group.quaternion));
    const direction = this.playerGroup.position.clone().sub(drone.group.position).normalize();
    this.spawnProjectile(origin, direction, false, 118, 0xffa173, 0.48, 0.7);
  }

  private spawnProjectile(
    origin: THREE.Vector3,
    direction: THREE.Vector3,
    friendly: boolean,
    speed: number,
    color: THREE.ColorRepresentation,
    radius: number,
    damage: number,
  ): void {
    const material = new THREE.SpriteMaterial({
      map: friendly ? this.textures.glow : this.textures.ember,
      color,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
    });
    const sprite = new THREE.Sprite(material);
    sprite.position.copy(origin);
    sprite.scale.setScalar(friendly ? 3.6 : 3.1);
    this.world.add(sprite);
    this.projectiles.push({
      sprite,
      velocity: direction.clone().multiplyScalar(speed),
      life: friendly ? 1.8 : 2.6,
      damage,
      radius,
      friendly,
    });
  }

  private triggerPulse(): void {
    this.player.pulseCooldown = 5.5;
    this.state.energy = Math.max(0, this.state.energy - 24);
    this.audio.pulse();
    this.spawnBurst(this.playerGroup.position, 0x7ef7ff, 20, 36, 0.44);
    this.showToast('Pulse wave unleashed.', 1.4);

    for (const drone of this.drones) {
      if (!drone.alive) {
        continue;
      }
      if (drone.group.position.distanceTo(this.playerGroup.position) < 38) {
        drone.health -= 2;
        drone.velocity.add(
          drone.group.position
            .clone()
            .sub(this.playerGroup.position)
            .normalize()
            .multiplyScalar(38),
        );
        if (drone.health <= 0) {
          this.destroyDrone(drone);
        }
      }
    }
  }

  private spawnContrail(): void {
    const positions = [
      this.engineLeft.getWorldPosition(new THREE.Vector3()),
      this.engineRight.getWorldPosition(new THREE.Vector3()),
    ];
    const backward = new THREE.Vector3(0, 0, 1)
      .applyQuaternion(this.playerGroup.quaternion)
      .normalize();

    for (const position of positions) {
      this.spawnParticle(
        position,
        backward
          .clone()
          .multiplyScalar(18 + Math.random() * 8)
          .add(
            new THREE.Vector3(
              THREE.MathUtils.randFloatSpread(2.4),
              THREE.MathUtils.randFloatSpread(2.4),
              THREE.MathUtils.randFloatSpread(2.4),
            ),
          ),
        0x7ef7ff,
        0.42,
        0.12,
      );
    }
  }

  private spawnBurst(
    position: THREE.Vector3,
    color: THREE.ColorRepresentation,
    count: number,
    speed: number,
    size: number,
  ): void {
    for (let index = 0; index < count; index += 1) {
      const direction = new THREE.Vector3(
        THREE.MathUtils.randFloatSpread(2),
        THREE.MathUtils.randFloatSpread(2),
        THREE.MathUtils.randFloatSpread(2),
      )
        .normalize()
        .multiplyScalar(speed * (0.45 + Math.random() * 0.55));
      this.spawnParticle(
        position,
        direction,
        color,
        0.5 + Math.random() * 0.45,
        size,
        0.2 + Math.random() * 0.28,
      );
    }
  }

  private spawnParticle(
    position: THREE.Vector3,
    velocity: THREE.Vector3,
    color: THREE.ColorRepresentation,
    life: number,
    scale: number,
    growth = 0.24,
  ): void {
    const mesh = new THREE.Mesh(
      this.particleGeometry,
      new THREE.MeshBasicMaterial({
        color,
        transparent: true,
        opacity: 0.88,
      }),
    );
    mesh.position.copy(position);
    mesh.scale.setScalar(scale);
    this.world.add(mesh);
    this.particles.push({
      mesh,
      velocity,
      life,
      maxLife: life,
      growth,
    });
  }

  private awardScore(baseScore: number): void {
    const gained = Math.round(baseScore * this.state.combo);
    this.state.score += gained;
    this.state.combo = Math.min(5.5, this.state.combo + 0.18);
    this.state.comboTime = 5.2;
  }

  private destroyDrone(drone: Drone): void {
    drone.alive = false;
    drone.group.visible = false;
    drone.respawn = 8 + Math.random() * 5;
    this.state.drones += 1;
    this.awardScore(210);
    this.spawnBurst(drone.group.position, 0xffa977, 16, 22, 0.28);
    this.audio.explosion();
    this.showToast('Sentry drone erased.', 1.2);
  }

  private damagePlayer(amount: number, message: string): void {
    this.state.hull = Math.max(0, this.state.hull - amount);
    this.player.hitFlash = 1;
    this.audio.damage();
    this.showToast(message, 1.15);
  }

  private removeProjectile(index: number): void {
    const [projectile] = this.projectiles.splice(index, 1);
    this.world.remove(projectile.sprite);
    (projectile.sprite.material as THREE.SpriteMaterial).dispose();
  }

  private removeParticle(index: number): void {
    const [particle] = this.particles.splice(index, 1);
    this.world.remove(particle.mesh);
    (particle.mesh.material as THREE.MeshBasicMaterial).dispose();
  }

  private finishRun(): void {
    this.isRunning = false;
    this.shell.classList.remove('is-live');
    this.hud.gameOver.classList.add('overlay--visible');
    this.hud.finalScore.textContent = `${Math.round(this.state.score)}`;
    this.hud.finalRank.textContent = this.rankForScore(this.state.score);
    this.hud.finalStyle.textContent = this.styleForRun();
    this.hud.finalBrief.textContent = this.missionComplete
      ? 'You cleared the mission grid and turned the sector into your playground.'
      : 'The frontier pushed back, but the next run is one launch away.';
    this.showToast('Hull lost. Queue another run.', 2.6);
  }

  private rankForScore(score: number): string {
    if (score < 400) {
      return 'Sky Cadet';
    }
    if (score < 1200) {
      return 'Ring Runner';
    }
    if (score < 2200) {
      return 'Cloud Hunter';
    }
    if (score < 3600) {
      return 'Aether Ace';
    }
    return 'Frontier Legend';
  }

  private styleForRun(): string {
    if (this.state.drones >= this.state.crystals && this.state.drones >= this.state.rings) {
      return 'Drone breaker';
    }
    if (this.state.rings >= this.state.crystals) {
      return 'Drift-line artist';
    }
    return 'Crystal thief';
  }

  private currentSector(): string {
    let nearest = this.islands[0];
    let nearestDistance = Number.POSITIVE_INFINITY;

    for (const island of this.islands) {
      const distance = island.position.distanceToSquared(this.playerGroup.position);
      if (distance < nearestDistance) {
        nearest = island;
        nearestDistance = distance;
      }
    }

    return nearest.name;
  }

  private nextMissionHint(): string {
    if (this.missionComplete) {
      return 'Mission complete. Keep free-roaming for style points.';
    }
    if (this.state.crystals < CRYSTAL_TARGET) {
      return 'Crystals recharge your ship fast. Dive low and collect them.';
    }
    if (this.state.drones < DRONE_TARGET) {
      return 'The drones are the threat. Push the fight into their patrol lanes.';
    }
    return 'Thread the glowing rings to finish the mission grid.';
  }

  private refreshHud(force: boolean): void {
    if (!force && !this.hasStarted && this.hud.startScreen.classList.contains('overlay--visible')) {
      return;
    }

    this.hud.scoreValue.textContent = `${Math.round(this.state.score)}`;
    this.hud.rankValue.textContent = this.rankForScore(this.state.score);
    this.hud.comboValue.textContent = `${this.state.combo.toFixed(1)}x`;
    this.hud.objectiveCrystal.textContent = `${this.state.crystals} / ${CRYSTAL_TARGET}`;
    this.hud.objectiveDrone.textContent = `${this.state.drones} / ${DRONE_TARGET}`;
    this.hud.objectiveRing.textContent = `${this.state.rings} / ${RING_TARGET}`;
    this.hud.missionStatus.textContent = this.nextMissionHint();
    this.hud.messageLine.textContent = this.toastTimer > 0 ? this.toastMessage : this.nextMissionHint();
    this.hud.sectorValue.textContent = this.currentSector();
    this.hud.speedValue.textContent = `${this.player.velocity.length().toFixed(0)} u/s`;
    this.hud.altitudeValue.textContent = `${Math.round(this.playerGroup.position.y)} m`;
    this.hud.threatValue.textContent = `Threat ${Math.round(this.state.threat)}%`;
    this.hud.crystalCount.textContent = `${this.state.crystals}`;
    this.hud.droneCount.textContent = `${this.state.drones}`;
    this.hud.ringCount.textContent = `${this.state.rings}`;
    this.hud.hullMeter.style.width = `${this.state.hull}%`;
    this.hud.energyMeter.style.width = `${this.state.energy}%`;
    this.hud.boostMeter.style.width = `${this.state.boost}%`;
  }

  private drawRadar(): void {
    const ctx = this.radarContext;
    const canvas = this.hud.radar;
    const width = canvas.width;
    const height = canvas.height;
    const centerX = width / 2;
    const centerY = height / 2;
    const radius = Math.min(width, height) * 0.38;
    const sweepAngle = this.state.elapsed * 1.2;
    const heading = this.player.yaw;

    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = 'rgba(4, 12, 17, 0.94)';
    ctx.fillRect(0, 0, width, height);

    ctx.strokeStyle = 'rgba(126, 247, 255, 0.1)';
    ctx.lineWidth = 2;
    for (const ratio of [0.3, 0.58, 0.86]) {
      ctx.beginPath();
      ctx.arc(centerX, centerY, radius * ratio, 0, TAU);
      ctx.stroke();
    }

    ctx.strokeStyle = 'rgba(126, 247, 255, 0.08)';
    ctx.beginPath();
    ctx.moveTo(centerX - radius, centerY);
    ctx.lineTo(centerX + radius, centerY);
    ctx.moveTo(centerX, centerY - radius);
    ctx.lineTo(centerX, centerY + radius);
    ctx.stroke();

    ctx.strokeStyle = 'rgba(255, 200, 87, 0.24)';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.lineTo(centerX + Math.cos(sweepAngle) * radius, centerY + Math.sin(sweepAngle) * radius);
    ctx.stroke();

    const drawBlip = (position: THREE.Vector3, color: string, size: number): void => {
      const relative = position.clone().sub(this.playerGroup.position);
      const range = 320;
      if (relative.lengthSq() > range * range) {
        return;
      }

      const rx = relative.x * Math.cos(-heading) - relative.z * Math.sin(-heading);
      const ry = relative.x * Math.sin(-heading) + relative.z * Math.cos(-heading);
      const x = centerX + (rx / range) * radius;
      const y = centerY + (ry / range) * radius;
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.arc(x, y, size, 0, TAU);
      ctx.fill();
    };

    for (const island of this.islands) {
      drawBlip(island.position, 'rgba(126, 247, 255, 0.28)', 3.5);
    }
    for (const crystal of this.crystals) {
      if (crystal.active) {
        drawBlip(crystal.group.position, 'rgba(169, 255, 224, 0.92)', 3.2);
      }
    }
    for (const ring of this.rings) {
      if (ring.active) {
        drawBlip(ring.group.position, 'rgba(255, 200, 87, 0.95)', 3.8);
      }
    }
    for (const drone of this.drones) {
      if (drone.alive) {
        drawBlip(drone.group.position, 'rgba(255, 143, 92, 0.98)', 3.6);
      }
    }

    ctx.fillStyle = '#ecfff8';
    ctx.beginPath();
    ctx.moveTo(centerX, centerY - 10);
    ctx.lineTo(centerX + 7, centerY + 8);
    ctx.lineTo(centerX - 7, centerY + 8);
    ctx.closePath();
    ctx.fill();
  }

  private showToast(message: string, duration: number): void {
    this.toastMessage = message;
    this.toastTimer = duration;
    this.hud.toast.textContent = message;
    this.hud.toast.classList.add('is-visible');
  }

  private updateToast(delta: number): void {
    if (this.toastTimer <= 0) {
      return;
    }
    this.toastTimer = Math.max(0, this.toastTimer - delta);
    if (this.toastTimer === 0) {
      this.hud.toast.classList.remove('is-visible');
    }
  }
}
