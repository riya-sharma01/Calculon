import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';

const WIDTH = 400;
const HEIGHT = 340;
const CX = WIDTH / 2;
const CY = HEIGHT / 2;
const SCALE = 26;

export default function VectorSpace() {
  const svgRef = useRef(null);
  const layerRef = useRef({});
  const [v1, setV1] = useState({ x: 3, y: 2 });
  const [v2, setV2] = useState({ x: -1, y: 3 });

  // ---- One-time setup ----
  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const defs = svg.append('defs');
    [['m-teal', '#2de2c9'], ['m-gold', '#ffc93c'], ['m-violet', '#a78bfa']].forEach(([id, color]) => {
      defs.append('marker')
        .attr('id', id).attr('viewBox', '0 0 10 10')
        .attr('refX', 8).attr('refY', 5)
        .attr('markerWidth', 6).attr('markerHeight', 6)
        .attr('orient', 'auto-start-reverse')
        .append('path').attr('d', 'M0,0 L10,5 L0,10 z').attr('fill', color);
    });
    const glow = defs.append('filter').attr('id', 'vs-glow').attr('x', '-60%').attr('y', '-60%').attr('width', '220%').attr('height', '220%');
    glow.append('feGaussianBlur').attr('stdDeviation', 2.5).attr('result', 'blur');
    const merge = glow.append('feMerge');
    merge.append('feMergeNode').attr('in', 'blur');
    merge.append('feMergeNode').attr('in', 'SourceGraphic');

    const g = svg.append('g');

    for (let i = -7; i <= 7; i++) {
      g.append('line').attr('x1', CX + i * SCALE).attr('x2', CX + i * SCALE)
        .attr('y1', 10).attr('y2', HEIGHT - 10).attr('stroke', 'rgba(251,250,255,0.05)');
      g.append('line').attr('y1', CY + i * SCALE).attr('y2', CY + i * SCALE)
        .attr('x1', 10).attr('x2', WIDTH - 10).attr('stroke', 'rgba(251,250,255,0.05)');
    }
    g.append('line').attr('x1', 10).attr('x2', WIDTH - 10).attr('y1', CY).attr('y2', CY).attr('stroke', '#2a355c');
    g.append('line').attr('x1', CX).attr('x2', CX).attr('y1', 10).attr('y2', HEIGHT - 10).attr('stroke', '#2a355c');

    const dashed1 = g.append('line').attr('stroke', 'rgba(157,140,255,0.35)').attr('stroke-dasharray', '4,3');
    const dashed2 = g.append('line').attr('stroke', 'rgba(157,140,255,0.35)').attr('stroke-dasharray', '4,3');

    const arrow1 = g.append('line').attr('x1', CX).attr('y1', CY)
      .attr('stroke', '#2de2c9').attr('stroke-width', 2.5).attr('marker-end', 'url(#m-teal)');
    const arrow2 = g.append('line').attr('x1', CX).attr('y1', CY)
      .attr('stroke', '#ffc93c').attr('stroke-width', 2.5).attr('marker-end', 'url(#m-gold)');
    const arrowSum = g.append('line').attr('x1', CX).attr('y1', CY)
      .attr('stroke', '#a78bfa').attr('stroke-width', 2.5).attr('marker-end', 'url(#m-violet)')
      .attr('filter', 'url(#vs-glow)');

    const label1 = g.append('text').attr('fill', '#2de2c9').attr('font-size', 12).text('v1');
    const label2 = g.append('text').attr('fill', '#ffc93c').attr('font-size', 12).text('v2');
    const labelSum = g.append('text').attr('fill', '#a78bfa').attr('font-size', 12).text('v1+v2');

    layerRef.current = { dashed1, dashed2, arrow1, arrow2, arrowSum, label1, label2, labelSum };
  }, []);

  // ---- Animate on vector change ----
  useEffect(() => {
    const { dashed1, dashed2, arrow1, arrow2, arrowSum, label1, label2, labelSum } = layerRef.current;
    if (!arrow1) return;

    const toPx = (v) => [CX + v.x * SCALE, CY - v.y * SCALE];
    const [x1, y1] = toPx(v1);
    const [x2, y2] = toPx(v2);
    const sum = { x: v1.x + v2.x, y: v1.y + v2.y };
    const [sx, sy] = toPx(sum);

    const t = (sel) => sel.transition().duration(200).ease(d3.easeCubicOut);

    t(dashed1).attr('x1', x1).attr('y1', y1).attr('x2', sx).attr('y2', sy);
    t(dashed2).attr('x1', x2).attr('y1', y2).attr('x2', sx).attr('y2', sy);
    t(arrow1).attr('x2', x1).attr('y2', y1);
    t(arrow2).attr('x2', x2).attr('y2', y2);
    t(arrowSum).attr('x2', sx).attr('y2', sy);

    label1.transition().duration(200).attr('x', x1 + 6).attr('y', y1 - 6);
    label2.transition().duration(200).attr('x', x2 + 6).attr('y', y2 - 6);
    labelSum.transition().duration(200).attr('x', sx + 6).attr('y', sy - 6);
  }, [v1, v2]);

  const update = (setter) => (axis) => (e) => {
    const val = parseFloat(e.target.value);
    setter((v) => ({ ...v, [axis]: val }));
  };

  return (
    <div className="viz-panel">
      <div className="viz-title">Vector addition</div>
      <svg ref={svgRef} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%" style={{ maxWidth: 400, display: 'block', margin: '0 auto' }} />
      <div className="mono" style={{ textAlign: 'center', margin: '8px 0 14px', fontSize: '0.85rem', color: 'var(--chalk-dim)' }}>
        v1 = <span className="viz-readout">({v1.x}, {v1.y})</span> &nbsp;
        v2 = <span className="viz-readout">({v2.x}, {v2.y})</span> &nbsp;
        v1+v2 = <span className="viz-readout">({v1.x + v2.x}, {v1.y + v2.y})</span>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div>
          <label>v1.x</label>
          <input type="range" min={-6} max={6} step={1} value={v1.x} onChange={update(setV1)('x')} style={{ width: '100%' }} />
          <label>v1.y</label>
          <input type="range" min={-6} max={6} step={1} value={v1.y} onChange={update(setV1)('y')} style={{ width: '100%' }} />
        </div>
        <div>
          <label>v2.x</label>
          <input type="range" min={-6} max={6} step={1} value={v2.x} onChange={update(setV2)('x')} style={{ width: '100%' }} />
          <label>v2.y</label>
          <input type="range" min={-6} max={6} step={1} value={v2.y} onChange={update(setV2)('y')} style={{ width: '100%' }} />
        </div>
      </div>
    </div>
  );
}
