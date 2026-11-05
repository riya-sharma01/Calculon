// Display details for each domain: its color, symbol and a one-line list of topics.
// Matched by domain name (the names come from backend/src/main/resources/questions/*.json).
// The order of DOMAIN_META is the order domains are shown on the home page and the Domains page.
// If you add a new domain, add an entry here; until you do, it still shows up with a neutral color.

export const LEVELS = [
  { key: 'FOUNDATION', label: 'Foundation', blurb: 'Core definitions and one-step problems.' },
  { key: 'STANDARD', label: 'Standard', blurb: 'Typical exercises on each topic.' },
  { key: 'INTERMEDIATE', label: 'Intermediate', blurb: 'Problems that combine two or more ideas.' },
  { key: 'HARD', label: 'Hard', blurb: 'Longer problems with tricky setups.' },
  { key: 'ADVANCED', label: 'Advanced', blurb: 'The hardest questions in each domain.' },
];

export const DOMAIN_META = [
  {
    name: 'Algebra',
    glyph: 'x²',
    color: '#7C5CFF',
    tint: '#ECE7FF',
    topics: 'Sequences and series, binomial theorem, complex numbers, permutations and combinations, sets, induction.',
  },
  {
    name: 'Trigonometry',
    glyph: 'θ',
    color: '#12B5A0',
    tint: '#D9F5F0',
    topics: 'Identities, inverse trigonometry, equations, heights and distances.',
  },
  {
    name: 'Coordinate Geometry',
    glyph: '(x,y)',
    color: '#2F8CFF',
    tint: '#DDEBFF',
    topics: 'Straight lines, circles, parabola, ellipse and hyperbola.',
  },
  {
    name: 'Calculus',
    glyph: '∫',
    color: '#FF8A3D',
    tint: '#FFEBDB',
    topics: 'Limits, derivatives, integrals and differential equations.',
  },
  {
    name: 'Vectors, 3D Geometry & Matrices',
    glyph: '→',
    color: '#FF4F93',
    tint: '#FFE0EC',
    topics: 'Vector algebra, 3D geometry, matrices and determinants.',
  },
  {
    name: 'Probability & Statistics',
    glyph: 'P(A)',
    color: '#3DBE5B',
    tint: '#DDF5E3',
    topics: 'Probability, permutations and combinations, statistics.',
  },
];

const FALLBACKS = [
  { color: '#7C5CFF', tint: '#ECE7FF' },
  { color: '#FF8A3D', tint: '#FFEBDB' },
  { color: '#12B5A0', tint: '#D9F5F0' },
  { color: '#2F8CFF', tint: '#DDEBFF' },
];

function metaIndex(name) {
  return DOMAIN_META.findIndex((m) => m.name.toLowerCase() === String(name || '').toLowerCase());
}

/** Meta for a domain name, or a neutral fallback so an unknown domain never breaks the page. */
export function getDomainMeta(name, index = 0) {
  const i = metaIndex(name);
  if (i !== -1) return DOMAIN_META[i];
  const fb = FALLBACKS[index % FALLBACKS.length];
  return { name, glyph: '✦', color: fb.color, tint: fb.tint, topics: null };
}

/** Sort position for a domain name: known domains in DOMAIN_META order, unknown ones after them. */
export function domainOrder(name) {
  const i = metaIndex(name);
  return i === -1 ? DOMAIN_META.length : i;
}

export function levelIndex(difficulty) {
  const i = LEVELS.findIndex((l) => l.key === difficulty);
  return i === -1 ? 0 : i;
}
