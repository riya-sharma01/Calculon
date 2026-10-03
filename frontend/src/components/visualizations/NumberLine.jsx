import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';

const WIDTH = 620;
const HEIGHT = 140;
const N = 50;

function isPrime(n) {
  if (n < 2) return false;
  for (let i = 2; i * i <= n; i++) if (n % i === 0) return false;
  return true;
}

const COLOR_BASE = '#171f42';
const COLOR_PRIME = '#ffc93c';
const COLOR_MULTIPLE = '#a78bfa';
const COLOR_BOTH = '#ff6b8b';

export default function NumberLine() {
  const svgRef = useRef(null);
  const dotsRef = useRef(null);
  const [highlightMultiplesOf, setHighlightMultiplesOf] = useState(0); // 0 = off

  // ---- One-time setup: draw the line and all 50 dots once ----
  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const margin = { left: 24, right: 24 };
    const usableW = WIDTH - margin.left - margin.right;
    const step = usableW / (N - 1);
    const y = HEIGHT / 2;

    const g = svg.append('g');

    g.append('line')
      .attr('x1', margin.left).attr('x2', WIDTH - margin.right)
      .attr('y1', y).attr('y2', y)
      .attr('stroke', '#2a355c').attr('stroke-width', 2);

    const nums = d3.range(1, N + 1);

    const dots = g.selectAll('circle.num')
      .data(nums)
      .join('circle')
      .attr('class', 'num')
      .attr('cx', (n) => margin.left + (n - 1) * step)
      .attr('cy', y)
      .attr('fill', (n) => (isPrime(n) ? COLOR_PRIME : COLOR_BASE))
      .attr('r', (n) => (isPrime(n) ? 6 : 4))
      .attr('stroke', '#0a0e1f')
      .attr('stroke-width', 1);

    g.selectAll('text.num-label')
      .data(nums.filter((n) => n % 5 === 0 || n === 1))
      .join('text')
      .attr('class', 'num-label')
      .attr('x', (n) => margin.left + (n - 1) * step)
      .attr('y', y + 22)
      .attr('text-anchor', 'middle')
      .attr('fill', '#b6bbe0')
      .attr('font-size', 10)
      .attr('font-family', 'JetBrains Mono, monospace')
      .text((n) => n);

    dotsRef.current = dots;
  }, []);

  // ---- Animate color/radius when the multiple filter changes ----
  useEffect(() => {
    const dots = dotsRef.current;
    if (!dots) return;

    dots.transition().duration(200).ease(d3.easeCubicOut)
      .attr('fill', (n) => {
        const prime = isPrime(n);
        const multiple = highlightMultiplesOf > 1 && n % highlightMultiplesOf === 0;
        if (prime && multiple) return COLOR_BOTH;
        if (multiple) return COLOR_MULTIPLE;
        if (prime) return COLOR_PRIME;
        return COLOR_BASE;
      })
      .attr('r', (n) => {
        const prime = isPrime(n);
        const multiple = highlightMultiplesOf > 1 && n % highlightMultiplesOf === 0;
        return prime || multiple ? 6 : 4;
      });
  }, [highlightMultiplesOf]);

  return (
    <div className="viz-panel">
      <div className="viz-title">Primes & multiples</div>
      <svg ref={svgRef} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%" />
      <div className="mono" style={{ fontSize: '0.8rem', color: 'var(--chalk-dim)', marginBottom: 10 }}>
        <span style={{ color: COLOR_PRIME }}>● prime</span> &nbsp;
        <span style={{ color: COLOR_MULTIPLE }}>● multiple</span> &nbsp;
        <span style={{ color: COLOR_BOTH }}>● both</span>
      </div>
      <label>Highlight multiples of</label>
      <input
        type="range"
        min={0}
        max={12}
        step={1}
        value={highlightMultiplesOf}
        onChange={(e) => setHighlightMultiplesOf(parseInt(e.target.value, 10))}
        style={{ width: '100%' }}
      />
      <div className="mono" style={{ fontSize: '0.8rem', color: 'var(--chalk-dim)' }}>
        <span className="viz-readout">{highlightMultiplesOf > 1 ? highlightMultiplesOf : 'none'}</span>
      </div>
    </div>
  );
}
