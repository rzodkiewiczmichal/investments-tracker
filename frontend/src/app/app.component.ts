import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MenubarModule, ButtonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  menuItems: MenuItem[] = [
    {
      label: 'Portfolio',
      icon: 'pi pi-chart-pie',
      routerLink: '/portfolio'
    },
    {
      label: 'Add Position',
      icon: 'pi pi-plus',
      routerLink: '/positions/new'
    }
  ];
}
