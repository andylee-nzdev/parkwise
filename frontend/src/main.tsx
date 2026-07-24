import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import ParkWise from "./ParkWise.tsx";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <ParkWise />
  </StrictMode>,
)
