import FunctionGrapher from './FunctionGrapher';
import UnitCircle from './UnitCircle';
import VectorSpace from './VectorSpace';
import DistributionPlot from './DistributionPlot';
import { getDomainMeta } from '../../domainMeta';

// Keyed by the domain names in backend/src/main/resources/questions/*.json.
// Algebra and Coordinate Geometry have no matching chart yet, so they show none.
const BY_DOMAIN_NAME = {
  Calculus: FunctionGrapher,
  Trigonometry: UnitCircle,
  'Vectors, 3D Geometry & Matrices': VectorSpace,
  'Probability & Statistics': DistributionPlot,
};

export default function VisualizationPanel({ domainName }) {
  const Viz = BY_DOMAIN_NAME[domainName];
  if (!Viz) return null;

  return (
    <div className="card chalkboard" style={{ marginBottom: 28, '--tile': getDomainMeta(domainName).color }}>
      <Viz />
    </div>
  );
}
