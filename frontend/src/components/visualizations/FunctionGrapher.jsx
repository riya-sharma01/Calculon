import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';

const WIDTH = 560;
const HEIGHT = 340;
const MARGIN = { top: 20, right: 20, bottom: 30, left: 40 };

// f(x) = x^2 / 3, a friendly curve for showing tangent slope = derivative.
const f = (x) => (x * x) / 3;
const fPrime = (x) => (2 * x) / 3;

export default function FunctionGrapher() {
  const svgRef = useRef(null);
  const layerRef = useRef({});
  const [x0, setX0] = useState(1.5);

  // ---- One-time setup: grid, axes, static curve ----
  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const innerW = WIDTH - MARGIN.left - MARGIN.right;
    const innerH = HEIGHT - MARGIN.top - MARGIN.bottom;

    const defs = svg.append('defs');
    const glow = defs.append('filter').attr('id', 'fg-glow').attr('x', '-60%').attr('y', '-60%').attr('width', '220%').attr('height', '220%');
    glow.append('feGaussianBlur').attr('stdDeviation', 4).attr('result', 'blur');
    const merge = glow.append('feMerge');
    merge.append('feMergeNode').attr('in', 'blur');
    merge.append('feMergeNode').attr('in', 'SourceGraphic');

    const xScale = d3.scaleLinear().domain([-6, 6]).range([0, innerW]);
    const yScale = d3.scaleLinear().domain([-1, 12]).range([innerH, 0]);

    const g = svg.append('g').attr('transform', `translate(${MARGIN.left},${MARGIN.top})`);

    g.append('g')
      .selectAll('line.v')
      .data(xScale.ticks(12))
      .join('line')
      .attr('x1', (d) => xScale(d)).attr('x2', (d) => xScale(d))
      .attr('y1', 0).attr('y2', innerH)
      .attr('stroke', 'rgba(251,250,255,0.06)');

    g.append('g')
      .selectAll('line.h')
      .data(yScale.ticks(8))
      .join('line')
      .attr('x1', 0).attr('x2', innerW)
      .attr('y1', (d) => yScale(d)).attr('y2', (d) => yScale(d))
      .attr('stroke', 'rgba(251,250,255,0.06)');

    g.append('g')
      .attr('transform', `translate(0,${yScale(0)})`)
      .call(d3.axisBottom(xScale).ticks(6))
      .call((axis) => axis.selectAll('text').attr('fill', '#b6bbe0').attr('font-size', 11))
      .call((axis) => axis.selectAll('path,line').attr('stroke', '#2a355c'));

    g.append('g')
      .attr('transform', `translate(${xScale(0)},0)`)
      .call(d3.axisLeft(yScale).ticks(5))
      .call((axis) => axis.selectAll('text').attr('fill', '#b6bbe0').attr('font-size', 11))
      .call((axis) => axis.selectAll('path,line').attr('stroke', '#2a355c'));

    const line = d3.line().x((d) => xScale(d)).y((d) => yScale(f(d)));
    const points = d3.range(-6, 6.05, 0.1);

    g.append('path')
      .datum(points)
      .attr('fill', 'none')
      .attr('stroke', '#2de2c9')
      .attr('stroke-width', 2.5)
      .attr('d', line);

    const tangent = g.append('line')
      .attr('stroke', '#ffc93c')
      .attr('stroke-width', 2)
      .attr('stroke-dasharray', '5,4');

    const point = g.append('circle')
      .attr('r', 6.5)
      .attr('fill', '#ff6b8b')
      .attr('stroke', '#0a0e1f')
      .attr('stroke-width', 2)
      .attr('filter', 'url(#fg-glow)');

    const label = g.append('text')
      .attr('fill', '#f1efe7')
      .attr('font-size', 12)
      .attr('font-family', 'JetBrains Mono, monospace');

    layerRef.current = { xScale, yScale, tangent, point, label };
  }, []);

  // ---- Update on x0 change, animated ----
  useEffect(() => {
    const { xScale, yScale, tangent, point, label } = layerRef.current;
    if (!xScale) return;

    const slope = fPrime(x0);
    const y0 = f(x0);
    const tx1 = x0 - 3;
    const tx2 = x0 + 3;
    const ty1 = y0 + slope * (tx1 - x0);
    const ty2 = y0 + slope * (tx2 - x0);

    tangent.transition().duration(180).ease(d3.easeCubicOut)
      .attr('x1', xScale(tx1)).attr('y1', yScale(ty1))
      .attr('x2', xScale(tx2)).attr('y2', yScale(ty2));

    point.transition().duration(180).ease(d3.easeCubicOut)
      .attr('cx', xScale(x0)).attr('cy', yScale(y0));

    label
      .attr('x', xScale(x0) + 12)
      .attr('y', yScale(y0) - 12)
      .text(`(${x0.toFixed(1)}, ${y0.toFixed(2)})`);
  }, [x0]);

  return (
    <div className="viz-panel">
      <div className="viz-title">Tangent line & derivative</div>
      <svg ref={svgRef} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%" />
      <div style={{ marginTop: 10 }}>
        <label htmlFor="x0-slider">
          Point x = <span className="viz-readout">{x0.toFixed(2)}</span> &nbsp;·&nbsp;
          slope f&#39;(x) = <span className="viz-readout">{fPrime(x0).toFixed(2)}</span>
        </label>
        <input
          id="x0-slider"
          type="range"
          min={-5}
          max={5}
          step={0.1}
          value={x0}
          onChange={(e) => setX0(parseFloat(e.target.value))}
          style={{ width: '100%' }}
        />
      </div>
    </div>
  );
}
