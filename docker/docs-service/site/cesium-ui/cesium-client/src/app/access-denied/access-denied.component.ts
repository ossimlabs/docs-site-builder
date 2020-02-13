import { Component, OnInit } from '@angular/core';
import { RouteMonitorService } from '../core/services/route-monitor.service';

@Component({
  selector: 'app-access-denied',
  templateUrl: './access-denied.component.html',
  styleUrls: ['./access-denied.component.css']
})
export class AccessDeniedComponent implements OnInit {

  private previousURL: string;
  private previousRoute: string;

  constructor(private routeMonitorService: RouteMonitorService) {
    this.previousRoute = routeMonitorService.getRoute();
    this.previousURL = `${window.location.origin}/${this.previousRoute}`;
  }

  ngOnInit() {
  }


  hasPreviousRoute(): boolean {
    return typeof this.previousRoute !== 'undefined';
  }
}
