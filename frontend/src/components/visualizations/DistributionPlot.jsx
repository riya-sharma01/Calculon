import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';

const WIDTH = 560;
const HEIGHT = 300;
const MARGIN = { top: 20, right: 20, bottom: 30, left: 20 };

function normalPdf(x, mean, sd) {
  return (1 / (sd * Math.sqrt(2 * Math.PI))) * Math.exp(-0.5 * ((x - mean) / sd) ** 2);
}

export default function DistributionPlot() {
  const svgRef = useRef(null);
  const layerRef = useRef({});
  const [mean, setMean] = useState(0);
  const [sd, setSd] = useState(1);

  const innerW = WIDTH - MARGIN.left - MARGIN.right;
  const innerH = HEIGHT - MARGIN.top - MARGIN.bottom;
  const xScale = d3.scaleLinear().domain([-8, 8]).range([0, innerW]);

  // ---- One-time setup ----
  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const g = svg.append('g').attr('transform', `translate(${MARGIN.left},${MARGIN.top})`);

    g.append('g')
      .attr('transform', `translate(0,${innerH})`)
      .call(d3.axisBottom(xScale).ticks(8))
      .call((axis) => axis.selectAll('text').attr('fill', '#b6bbe0').attr('font-size', 11))
      .call((axis) => axis.selectAll('path,line').attr('stroke', '#2a355c'));

    const areaPath = g.append('path').attr('fill', 'rgba(79,209,197,0.18)');
    const linePath = g.append('path').attr('fill', 'none').attr('stroke', '#2de2c9').attr('stroke-width', 2.5);
    const meanLine = g.append('line').attr('y2', innerH).attr('stroke', '#ffc93c').attr('stroke-width', 2).attr('stroke-dasharray', '5,4');

    layerRef.current = { areaPath, linePath, meanLine };
  }, []);

  // ---- Animate on mean/sd change ----
  useEffect(() => {
    const { areaPath, linePath, meanLine } = layerRef.current;
    if (!areaPath) return;

    const points = d3.range(-8, 8.05, 0.05).map((x) => ({ x, y: normalPdf(x, mean, sd) }));
    const maxY = d3.max(points, (d) => d.y) || 0.5;
    const yScale = d3.scaleLinear().domain([0, maxY * 1.15]).range([innerH, 0]);

    const area = d3.area()
      .x((d) => xScale(d.x))
      .y0(innerH)
      .y1((d) => yScale(d.y))
      .curve(d3.curveBasis);

    const line = d3.line()
      .x((d) => xScale(d.x))
      .y((d) => yScale(d.y))
      .curve(d3.curveBasis);

    areaPath.transition().duration(180).ease(d3.easeCubicOut).attr('d', area(points));
    linePath.transition().duration(180).ease(d3.easeCubicOut).attr('d', line(points));
    meanLine.transition().duration(180).ease(d3.easeCubicOut)
      .attr('x1', xScale(mean)).attr('x2', xScale(mean))
      .attr('y2', yScale(normalPdf(mean, mean, sd)));
  }, [mean, sd]);

  return (
    <div className="viz-panel">
      <div className="viz-title">Normal distribution</div>
      <svg ref={svgRef} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%" />
      <div className="mono" style={{ textAlign: 'center', margin: '8px 0 14px', fontSize: '0.85rem', color: 'var(--chalk-dim)' }}>
        μ = <span className="viz-readout">{mean.toFixed(1)}</span> &nbsp;
        σ = <span className="viz-readout">{sd.toFixed(1)}</span>
      </div>
      <label>Mean (μ)</label>
      <input type="range" min={-4} max={4} step={0.1} value={mean} onChange={(e) => setMean(parseFloat(e.target.value))} style={{ width: '100%' }} />
      <label>Standard deviation (σ)</label>
      <input type="range" min={0.4} max={3} step={0.1} value={sd} onChange={(e) => setSd(parseFloat(e.target.value))} style={{ width: '100%' }} />
    </div>
  );
}
