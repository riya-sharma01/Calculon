import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';

const SIZE = 340;
const R = 120;
const CX = SIZE / 2;
const CY = SIZE / 2;

export default function UnitCircle() {
  const svgRef = useRef(null);
  const layerRef = useRef({});
  const [angleDeg, setAngleDeg] = useState(45);

  const angleRad = (angleDeg * Math.PI) / 180;
  const cos = Math.cos(angleRad);
  const sin = Math.sin(angleRad);

  // ---- One-time setup ----
  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const defs = svg.append('defs');
    const glow = defs.append('filter').attr('id', 'uc-glow').attr('x', '-60%').attr('y', '-60%').attr('width', '220%').attr('height', '220%');
    glow.append('feGaussianBlur').attr('stdDeviation', 3.5).attr('result', 'blur');
    const merge = glow.append('feMerge');
    merge.append('feMergeNode').attr('in', 'blur');
    merge.append('feMergeNode').attr('in', 'SourceGraphic');

    const g = svg.append('g');

    g.append('line').attr('x1', CX - R - 20).attr('x2', CX + R + 20).attr('y1', CY).attr('y2', CY).attr('stroke', '#2a355c');
    g.append('line').attr('x1', CX).attr('x2', CX).attr('y1', CY - R - 20).attr('y2', CY + R + 20).attr('stroke', '#2a355c');

    g.append('circle').attr('cx', CX).attr('cy', CY).attr('r', R)
      .attr('fill', 'none').attr('stroke', '#2de2c9').attr('stroke-width', 2);

    const radius = g.append('line').attr('x1', CX).attr('y1', CY)
      .attr('stroke', '#ffc93c').attr('stroke-width', 2);

    const cosLine = g.append('line').attr('y1', CY)
      .attr('stroke', '#ff6b8b').attr('stroke-width', 2).attr('stroke-dasharray', '4,3');
    const sinLine = g.append('line')
      .attr('stroke', '#a78bfa').attr('stroke-width', 2).attr('stroke-dasharray', '4,3');

    const point = g.append('circle').attr('r', 6.5)
      .attr('fill', '#ffc93c').attr('stroke', '#0a0e1f').attr('stroke-width', 2)
      .attr('filter', 'url(#uc-glow)');

    g.append('text').attr('x', CX + R + 26).attr('y', CY + 4).attr('fill', '#ff6b8b').attr('font-size', 11).text('cos');
    g.append('text').attr('x', CX - 6).attr('y', CY - R - 26).attr('fill', '#a78bfa').attr('font-size', 11).text('sin');

    layerRef.current = { radius, cosLine, sinLine, point };
  }, []);

  // ---- Animate on angle change ----
  useEffect(() => {
    const { radius, cosLine, sinLine, point } = layerRef.current;
    if (!radius) return;

    const px = CX + R * cos;
    const py = CY - R * sin;

    radius.transition().duration(150).ease(d3.easeCubicOut).attr('x2', px).attr('y2', py);
    cosLine.transition().duration(150).ease(d3.easeCubicOut).attr('x1', CX).attr('x2', px).attr('y2', CY);
    sinLine.transition().duration(150).ease(d3.easeCubicOut).attr('x1', px).attr('x2', px).attr('y1', CY).attr('y2', py);
    point.transition().duration(150).ease(d3.easeCubicOut).attr('cx', px).attr('cy', py);
  }, [angleDeg]);

  return (
    <div className="viz-panel">
      <div className="viz-title">The unit circle</div>
      <svg ref={svgRef} viewBox={`0 0 ${SIZE} ${SIZE}`} width="100%" style={{ maxWidth: 340, display: 'block', margin: '0 auto' }} />
      <div className="mono" style={{ textAlign: 'center', marginBottom: 10, color: 'var(--chalk-dim)', fontSize: '0.85rem' }}>
        θ = <span className="viz-readout">{angleDeg}°</span> &nbsp;
        cos(θ) = <span className="viz-readout">{cos.toFixed(2)}</span> &nbsp;
        sin(θ) = <span className="viz-readout">{sin.toFixed(2)}</span>
      </div>
      <label htmlFor="angle-slider">Angle θ</label>
      <input
        id="angle-slider"
        type="range"
        min={0}
        max={360}
        step={1}
        value={angleDeg}
        onChange={(e) => setAngleDeg(parseInt(e.target.value, 10))}
        style={{ width: '100%' }}
      />
    </div>
  );
}
