import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'portfolio',
    pathMatch: 'full'
  },
  {
    path: 'portfolio',
    loadComponent: () => import('./features/portfolio/portfolio-view/portfolio-view.component')
      .then(m => m.PortfolioViewComponent)
  },
  {
    path: 'positions/new',
    loadComponent: () => import('./features/positions/position-entry/position-entry.component')
      .then(m => m.PositionEntryComponent)
  },
  {
    path: 'positions/:id',
    loadComponent: () => import('./features/positions/position-details/position-details.component')
      .then(m => m.PositionDetailsComponent)
  },
  {
    path: '**',
    redirectTo: 'portfolio'
  }
];
