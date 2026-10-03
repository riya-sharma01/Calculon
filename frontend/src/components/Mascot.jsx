export default function Mascot({ emoji = '🦉', say, size = 'md' }) {
  return (
    <div className={`mascot mascot-${size}`}>
      <span className="mascot-face">{emoji}</span>
      {say && <div className="mascot-bubble">{say}</div>}
    </div>
  );
}
