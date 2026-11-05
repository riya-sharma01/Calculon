import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
// The charts call selection.transition(). That method is added by d3-transition as a side effect of
// importing it. Without this line the production build drops the module and every chart crashes
// ("transition is not a function"), even though `npm run dev` works fine.
import 'd3-transition';
import './styles/global.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
