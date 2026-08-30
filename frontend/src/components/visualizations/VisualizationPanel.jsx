import FunctionGrapher from './FunctionGrapher';
import UnitCircle from './UnitCircle';
import VectorSpace from './VectorSpace';
import DistributionPlot from './DistributionPlot';
import NumberLine from './NumberLine';

const BY_DOMAIN_NAME = {
  Calculus: FunctionGrapher,
  Trigonometry: UnitCircle,
  'Linear Algebra': VectorSpace,
  Probability: DistributionPlot,
  'Number Theory': NumberLine,
};

export default function VisualizationPanel({ domainName }) {
  const Viz = BY_DOMAIN_NAME[domainName];
  if (!Viz) return null;

  return (
    <div className="card" style={{ marginBottom: 28 }}>
      <Viz />
    </div>
  );
}
