import { Component, NgZone, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { RxStompService } from '@stomp/ng2-stompjs';

import { DataService } from './core/services/data.service';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { environment } from '../environments/environment';
import { RouteMonitorService } from './core/services/route-monitor.service';



@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements AfterViewChecked{
  @ViewChild('resizeBox', { static: false }) resizeBox: ElementRef;
  @ViewChild('mapView', { static: false }) mapView: ElementRef;
  @ViewChild('dragHandleRight', { static: false }) dragHandleRight: ElementRef;
  @ViewChild('dragArrowRight', { static: false }) dragArrowRight: ElementRef;

  private _collapsed: boolean = false;
  private _previousWidth: number = 0;
  private _dragHandleRightPosition: { x: number, y: number } = { x: 0, y: 0 };
  private _dragHandleOffset: number = 0;
  private bannerContent = environment.CLASSIFICATION_BANNER;

  get dragHandleOffset(): number {
    return this._dragHandleOffset;
  }

  set dragHandleOffset(value) {
    this._dragHandleOffset = value;
  }

  get dragHandleRightPosition(): { x: number, y: number } {
    return this._dragHandleRightPosition;
  }

  set dragHandleRightPosition(value) {
    this._dragHandleRightPosition = value;
  }

  get previousWidth(): number {
    return this._previousWidth;
  }

  set previousWidth(value) {
    this._previousWidth = value;
  }

  get collapsed(): boolean {
    return this._collapsed;
  }

  set collapsed(value) {
    this._collapsed = value;
  }

  get resizeBoxElement(): HTMLElement {
    return this.resizeBox.nativeElement;
  }

  get mapViewElement(): HTMLElement {
    return this.mapView.nativeElement;
  }

  get dragHandleRightElement(): HTMLElement {
    return this.dragHandleRight.nativeElement;
  }

  get dragArrowRightElement(): HTMLElement {
    return this.dragArrowRight.nativeElement;
  }


  constructor(
    private http: HttpClient,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private rxStompService: RxStompService,
    private dataService: DataService,
    private ngZone: NgZone) {

     
     
  }
 
  ngAfterViewChecked() {
    this.dragHandleOffset = this.resizeBoxElement.getBoundingClientRect().width;
    this.setMapSize();
    this.setHandleTransform();

    const baseCallurl = '/czml/packets?session=';
    const baseStompUrl = '/topic/czml/';
    const defaultSession = 'default';
    const keepAlive = environment.KEEP_ALIVE;

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const sessions = this.activatedRoute.snapshot.queryParamMap.getAll('session');

        let sessionUrls = [];

        if (!!sessions && sessions.length > 0) {
          sessions.forEach((session) => {
            sessionUrls.push({
              session: session,
              callUrl: baseCallurl + session,
              stompUrl: baseStompUrl + session
            });
          });
        } else {
          sessionUrls.push({
            session: defaultSession,
            callUrl: baseCallurl + defaultSession,
            stompUrl: baseStompUrl + defaultSession
          });
        }

        sessionUrls.forEach((sessionUrl) => {
          this.http.get(sessionUrl.callUrl, { responseType: 'text' }).subscribe((message: string) => {
            this.dataService.sendPacketData(sessionUrl.session, message);
          });

          this.rxStompService.watch(sessionUrl.stompUrl).subscribe(message => {
            let data = message.body;
            
            if(data === keepAlive){
              this.dataService.refreshUserSession(sessionUrl.session).subscribe(
                res => console.log('HTTP response', res),
                err => console.log('HTTP Error', err),
                () => console.log('HTTP request completed.'));
            }else{
              if (data === '') {
                data = null;
              }
              this.dataService.sendPacketData(sessionUrl.session, data);
            }
            
          });
        });
      }
    });
  }

  setMapSize() {
    const resizeBoxRect = this.resizeBoxElement.getBoundingClientRect();
    this.mapViewElement.style.width = `calc(100% - ${resizeBoxRect.width}px)`;
  }

  setHandleTransform() {
    const resizeBoxRect = this.resizeBoxElement.getBoundingClientRect();
    const dragRect = this.dragHandleRightElement.getBoundingClientRect();
    const translateX = resizeBoxRect.width - dragRect.width + 4; // + 4 to avoid scrollbar

    this.dragHandleRightElement.style.transform = `translate3d(${translateX}px, 0, 0)`;
    this.dragArrowRightElement.style.transform = `translate3d(${translateX}px, 0, 0)`;
  }

  resize() {
    const dragRect = this.dragHandleRightElement.getBoundingClientRect();
    const targetRect = this.resizeBoxElement.getBoundingClientRect();
    const width = dragRect.left - targetRect.left + dragRect.width;
    this.setWidths(width);
    this.setHandleTransform();

    // Make sure the arrow direction switches if the user drags out from
    // the collapsed state
    if (this.collapsed) {
      this.collapsed = false;
    }
  }

  setWidths(width) {
    this.resizeBoxElement.style.width = width + 'px';
    this.mapViewElement.style.width = `calc(100% - ${width}px)`;
  }

  dragMove() {
    this.ngZone.runOutsideAngular(() => {
      this.resize();
    });
  }

  fitToContent() {
    this.resizeBoxElement.style.width = 'min-content';
    const newDragHandlePos = this.resizeBoxElement.getBoundingClientRect().width - this.dragHandleOffset;
    this.dragHandleRightPosition = { x: newDragHandlePos, y: 0 };
    this.setMapSize();
    this.setHandleTransform();
  }

  toggleTree() {
    this.collapsed = !this.collapsed;

    if (this.collapsed) {
      // Set the drag handle to the left side of the screen
      // Note: The x/y coords here are relative to its initial position. They are NOT
      // absolute screen coords.
      this.dragHandleRightPosition = { x: 0 - this.dragHandleOffset, y: 0 };
      this.previousWidth = 0;
    } else {
      this.dragHandleRightPosition = { x: this.previousWidth - this.dragHandleOffset, y: 0 };
    }

    const width = this.resizeBoxElement.getBoundingClientRect().right;
    this.setWidths(this.previousWidth);
    this.setHandleTransform();
    this.previousWidth = width;
  }
}
