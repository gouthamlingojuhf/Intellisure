import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'is-hero-visual',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="hero-visual" role="region" aria-label="Live Risk & Portfolio Intelligence Visualizer">
      <!-- Ambient background grid & glow -->
      <div class="visual-ambient-grid" aria-hidden="true"></div>
      <div class="visual-glow" aria-hidden="true"></div>

      <!-- Top HUD Header -->
      <div class="hud-top">
        <div class="hud-identity">
          <span class="hud-kicker">Risk Intelligence Engine</span>
          <strong class="hud-status">Active Portfolio Guard</strong>
        </div>
        <div class="hud-live-badge" aria-label="Status: Live System Telemetry">
          <span class="pulse-indicator" aria-hidden="true"></span>
          <span>Live Telemetry</span>
        </div>
      </div>

      <!-- Center Radar & Confidence Gauge -->
      <div class="radar-container" aria-hidden="true">
        <!-- SVG Radar & Metric Rings -->
        <svg class="radar-svg" viewBox="0 0 280 280">
          <!-- Concentric range rings -->
          <circle cx="140" cy="140" r="130" class="ring outer" />
          <circle cx="140" cy="140" r="95" class="ring mid" />
          <circle cx="140" cy="140" r="60" class="ring inner" />
          
          <!-- Crosshair axes -->
          <line x1="140" y1="10" x2="140" y2="270" class="crosshair" />
          <line x1="10" y1="140" x2="270" y2="140" class="crosshair" />

          <!-- Dynamic score ring -->
          <circle
            cx="140"
            cy="140"
            r="95"
            class="score-arc"
            stroke-dasharray="597"
            stroke-dashoffset="65"
            transform="rotate(-90 140 140)"
          />

          <!-- Rotating Radar Sweep arm -->
          <g class="radar-sweep-arm">
            <line x1="140" y1="140" x2="140" y2="10" class="sweep-line" />
            <polygon points="140,140 140,10 190,40" class="sweep-fade" />
          </g>

          <!-- Detected Risk & Policy Nodes -->
          <circle cx="95" cy="85" r="5" class="radar-node node-stable" />
          <circle cx="190" cy="105" r="6" class="radar-node node-active" />
          <circle cx="170" cy="195" r="4" class="radar-node node-stable" />
          <circle cx="80" cy="180" r="5" class="radar-node node-priority" />
        </svg>

        <!-- Center Numerical readout -->
        <div class="gauge-center-content">
          <span class="confidence-number">94</span>
          <span class="confidence-label">Confidence Index</span>
        </div>
      </div>

      <!-- Telemetry Metrics Strip -->
      <div class="telemetry-strip" role="list" aria-label="Key operational telemetry">
        <div class="telemetry-item" role="listitem">
          <span class="telemetry-title">Exposure Score</span>
          <strong class="telemetry-value">Low-Moderate</strong>
          <span class="telemetry-trend positive">&darr; 2.4% vs baseline</span>
        </div>
        <div class="telemetry-item" role="listitem">
          <span class="telemetry-title">Underwriting Gate</span>
          <strong class="telemetry-value">Automated Bind</strong>
          <span class="telemetry-trend neutral">&plusmn; Zero-touch tier</span>
        </div>
        <div class="telemetry-item" role="listitem">
          <span class="telemetry-title">Recovery Readiness</span>
          <strong class="telemetry-value">Vetted Dispatch</strong>
          <span class="telemetry-trend positive">&check; 4 certified vendors</span>
        </div>
      </div>

      <!-- Activity Spark Bars -->
      <div class="spark-bars-container" aria-hidden="true">
        <span class="spark-label">Real-time Decision Stream</span>
        <div class="spark-bars">
          <span style="height: 48%"></span>
          <span style="height: 64%"></span>
          <span style="height: 52%"></span>
          <span style="height: 82%"></span>
          <span style="height: 70%"></span>
          <span style="height: 94%"></span>
          <span style="height: 60%"></span>
          <span style="height: 78%"></span>
          <span style="height: 88%"></span>
          <span style="height: 66%"></span>
          <span style="height: 92%"></span>
          <span style="height: 84%"></span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }

    .hero-visual {
      position: relative;
      border-radius: 14px;
      padding: 32px;
      background: radial-gradient(circle at 80% 15%, rgba(254, 48, 130, 0.16), transparent 45%),
                  linear-gradient(150deg, #131113 0%, #1e131b 60%, #291220 100%);
      color: #ffffff;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.35);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      gap: 26px;
    }

    .visual-ambient-grid {
      position: absolute;
      inset: 0;
      background-image: 
        linear-gradient(to right, rgba(255, 255, 255, 0.03) 1px, transparent 1px),
        linear-gradient(to bottom, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
      background-size: 28px 28px;
      pointer-events: none;
    }

    .visual-glow {
      position: absolute;
      top: -60px;
      right: -60px;
      width: 220px;
      height: 220px;
      border-radius: 50%;
      background: rgba(117, 1, 63, 0.45);
      filter: blur(60px);
      pointer-events: none;
    }

    .hud-top {
      position: relative;
      z-index: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      padding-bottom: 16px;
    }

    .hud-identity {
      display: flex;
      flex-direction: column;
      gap: 3px;
    }

    .hud-kicker {
      font-size: 9px;
      text-transform: uppercase;
      letter-spacing: 0.14em;
      color: #d19ab3;
      font-weight: 700;
    }

    .hud-status {
      font-size: 15px;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: #ffffff;
    }

    .hud-live-badge {
      display: flex;
      align-items: center;
      gap: 7px;
      padding: 5px 10px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid rgba(255, 255, 255, 0.14);
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: #9fe0be;
    }

    .pulse-indicator {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #22c55e;
      box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.25);
      animation: pulseDot 2s infinite ease-in-out;
    }

    @keyframes pulseDot {
      0%, 100% {
        opacity: 1;
        transform: scale(1);
      }
      50% {
        opacity: 0.6;
        transform: scale(1.15);
      }
    }

    .radar-container {
      position: relative;
      z-index: 1;
      width: 240px;
      height: 240px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .radar-svg {
      width: 100%;
      height: 100%;
    }

    .ring {
      fill: none;
      stroke: rgba(255, 255, 255, 0.09);
      stroke-width: 1;
    }
    .ring.outer { stroke-dasharray: 4 4; }
    .ring.mid { stroke: rgba(255, 255, 255, 0.14); }

    .crosshair {
      stroke: rgba(255, 255, 255, 0.07);
      stroke-width: 1;
      stroke-dasharray: 2 4;
    }

    .score-arc {
      fill: none;
      stroke: var(--fuchsia, #fe3082);
      stroke-width: 6;
      stroke-linecap: round;
      opacity: 0.95;
    }

    .radar-sweep-arm {
      transform-origin: 140px 140px;
      animation: rotateRadar 7s linear infinite;
    }

    .sweep-line {
      stroke: rgba(254, 48, 130, 0.7);
      stroke-width: 1.5;
    }

    .sweep-fade {
      fill: url(#sweepGradient);
      fill: rgba(254, 48, 130, 0.08);
    }

    @keyframes rotateRadar {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .radar-node {
      filter: drop-shadow(0 0 4px rgba(254, 48, 130, 0.8));
      animation: nodePulse 3s infinite ease-in-out;
    }
    .node-stable { fill: #22c55e; }
    .node-active { fill: var(--fuchsia, #fe3082); animation-delay: 1s; }
    .node-priority { fill: #eab308; animation-delay: 2s; }

    @keyframes nodePulse {
      0%, 100% { opacity: 0.8; transform: scale(1); }
      50% { opacity: 1; transform: scale(1.2); }
    }

    .gauge-center-content {
      position: absolute;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      pointer-events: none;
    }

    .confidence-number {
      font-size: 38px;
      font-weight: 800;
      letter-spacing: -0.04em;
      line-height: 1;
      color: #ffffff;
    }

    .confidence-label {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #c9bfca;
      margin-top: 4px;
    }

    .telemetry-strip {
      position: relative;
      z-index: 1;
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 12px;
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 9px;
      padding: 14px 16px;
    }

    .telemetry-item {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .telemetry-title {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.1em;
      text-transform: uppercase;
      color: #b7adb4;
    }

    .telemetry-value {
      font-size: 13px;
      font-weight: 700;
      color: #ffffff;
    }

    .telemetry-trend {
      font-size: 9px;
      font-weight: 600;
    }
    .telemetry-trend.positive { color: #4ade80; }
    .telemetry-trend.neutral { color: #e2e8f0; }

    .spark-bars-container {
      position: relative;
      z-index: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .spark-label {
      font-size: 8px;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #9c929a;
    }

    .spark-bars {
      display: flex;
      align-items: flex-end;
      gap: 5px;
      height: 38px;
      padding-bottom: 2px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .spark-bars span {
      flex: 1;
      border-radius: 2px 2px 0 0;
      background: linear-gradient(to top, var(--claret, #75013f), var(--fuchsia, #fe3082));
      opacity: 0.85;
      transition: height 0.3s ease;
    }

    @media (max-width: 640px) {
      .hero-visual {
        padding: 22px 18px;
      }
      .telemetry-strip {
        grid-template-columns: 1fr;
        gap: 10px;
      }
      .radar-container {
        width: 200px;
        height: 200px;
      }
      .confidence-number {
        font-size: 32px;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .radar-sweep-arm {
        animation: none;
      }
      .pulse-indicator, .radar-node {
        animation: none;
      }
      .spark-bars span {
        transition: none;
      }
    }
  `],
})
export class HeroVisualComponent {}
