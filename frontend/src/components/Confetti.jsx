const COLORS = ['#ffc93c', '#ff6b8b', '#2de2c9', '#a78bfa', '#a3ff5f', '#ff5fa2'];
const PIECES = 36;

export default function Confetti({ active }) {
  if (!active) return null;

  const pieces = Array.from({ length: PIECES }, (_, i) => {
    const angle = (360 / PIECES) * i + (Math.random() * 20 - 10);
    const distance = 120 + Math.random() * 160;
    const dx = Math.cos((angle * Math.PI) / 180) * distance;
    const dy = Math.sin((angle * Math.PI) / 180) * distance - 60;
    const color = COLORS[i % COLORS.length];
    const size = 6 + Math.random() * 6;
    const delay = Math.random() * 0.15;
    const duration = 0.9 + Math.random() * 0.5;
    const rotate = Math.random() * 720 - 360;
    return { id: i, dx, dy, color, size, delay, duration, rotate };
  });

  return (
    <div className="confetti-burst" aria-hidden="true">
      {pieces.map((p) => (
        <span
          key={p.id}
          className="confetti-piece"
          style={{
            '--dx': `${p.dx}px`,
            '--dy': `${p.dy}px`,
            '--rot': `${p.rotate}deg`,
            background: p.color,
            width: p.size,
            height: p.size * 1.4,
            animationDelay: `${p.delay}s`,
            animationDuration: `${p.duration}s`,
          }}
        />
      ))}
    </div>
  );
}
