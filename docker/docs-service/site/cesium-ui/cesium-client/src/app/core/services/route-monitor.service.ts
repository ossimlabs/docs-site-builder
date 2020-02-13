import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class RouteMonitorService {

  constructor(private router: Router) { }

  private route;

  setRoute(route) {
    this.route = route;
  }

  getRoute() {
    return this.route;
  }
}