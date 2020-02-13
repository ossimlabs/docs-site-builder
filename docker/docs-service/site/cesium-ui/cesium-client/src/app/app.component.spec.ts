import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { BannerComponent } from './classification-banner/banner.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatIconModule, MatTreeModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatNativeDateModule, MatInputModule } from '@angular/material';
import { LayerTreeComponent } from './layer-tree/layer-tree.component';
import { TimelineComponent } from './timeline/timeline.component';
import { AngularCesiumModule } from 'angular-cesium';
import { HttpClientModule } from '@angular/common/http';
import { RxStompService, rxStompServiceFactory, InjectableRxStompConfig } from '@stomp/ng2-stompjs';
import { RxStompConfig } from './rx-stomp.config';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Component } from '@angular/core';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  // Mocking the Cesium map drastically speeds up the tests
  @Component({ selector: 'app-cesium-map', template: '' })
  class MockCesiumMapComponent { }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        AngularCesiumModule.forRoot(),
        BrowserAnimationsModule,
        DragDropModule,
        HttpClientModule,
        MatCheckboxModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatNativeDateModule,
        MatTreeModule,
        RouterTestingModule
      ],
      declarations: [
        AppComponent,
        BannerComponent,
        MockCesiumMapComponent,
        LayerTreeComponent,
        TimelineComponent
      ],
      providers: [
        {
          provide: InjectableRxStompConfig,
          useValue: RxStompConfig
        },
        {
          provide: RxStompService,
          useFactory: rxStompServiceFactory,
          deps: [InjectableRxStompConfig]
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should get the resizeBoxElement', () => {
    const resizeElement = component.resizeBoxElement;
    expect(resizeElement instanceof HTMLElement).toBeTruthy();
  });

  it('should get the mapViewElement', () => {
    const mapViewElement = component.mapViewElement;
    expect(mapViewElement instanceof HTMLElement).toBeTruthy();
  });

  it('should get the dragHandleRightElement', () => {
    const dragHandleRightElement = component.dragHandleRightElement;
    expect(dragHandleRightElement instanceof HTMLElement).toBeTruthy();
  });

  it('should get the dragArrowRightElement', () => {
    const dragArrowRightElement = component.dragArrowRightElement;
    expect(dragArrowRightElement instanceof HTMLElement).toBeTruthy();
  });

  it('should toggle the layer tree closed when it\'s open', () => {
    component.collapsed = false;
    component.previousWidth = 0;
    component.dragHandleOffset = 10;
    component.toggleTree();
    expect(component.resizeBoxElement.style.width).toEqual('0px');
    expect(component.mapViewElement.style.width).toEqual('calc(100% - 0px)');
    expect(component.dragHandleRightPosition).toEqual({ x: -10, y: 0 });
    expect(component.collapsed).toBeTruthy();
  });

  it('should toggle the layer tree open when it\'s closed', () => {
    component.collapsed = true;
    component.previousWidth = 10;
    component.dragHandleOffset = 10;
    component.toggleTree();
    expect(component.resizeBoxElement.style.width).toEqual('10px');
    expect(component.mapViewElement.style.width).toEqual('calc(100% - 10px)');
    expect(component.dragHandleRightPosition).toEqual({ x: 0, y: 0 });
    expect(component.collapsed).toBeFalsy();
  });

  it('should trigger resizing when dragging occurs', () => {
    const resizeSpy = spyOn(component, 'resize');
    component.dragMove();
    expect(resizeSpy).toHaveBeenCalled();
  });

  it('should fit to content', () => {
    const setMapSizeSpy = spyOn(component, 'setMapSize');
    const setHandleTransformSpy = spyOn(component, 'setHandleTransform');
    component.fitToContent();
    expect(component.resizeBoxElement.style.width).toEqual('min-content');
    expect(setMapSizeSpy).toHaveBeenCalled();
    expect(setHandleTransformSpy).toHaveBeenCalled();
  });

  it('should set the layer tree and map widths', () => {
    component.setWidths(10);
    expect(component.resizeBoxElement.style.width).toEqual('10px');
    expect(component.mapViewElement.style.width).toEqual('calc(100% - 10px)');
  });

  it('should set the handle and arrow transform', () => {
    component.resizeBoxElement.style.width = '10px';
    component.dragHandleRightElement.style.width = '4px';
    component.setHandleTransform();
    expect(component.dragHandleRightElement.style.transform).toEqual('translate3d(10px, 0px, 0px)');
    expect(component.dragArrowRightElement.style.transform).toEqual('translate3d(10px, 0px, 0px)');
  });

  it('should set the map size', () => {
    component.resizeBoxElement.style.width = '10px';
    component.setMapSize();
    expect(component.mapViewElement.style.width).toEqual('calc(100% - 10px)');
  });

  it('it should set the widths when resizing', () => {
    const setHandleTransformSpy = spyOn(component, 'setHandleTransform');
    const dragHandleLeft = component.dragHandleRightElement.getBoundingClientRect().left;
    const dragHandleWidth = component.dragHandleRightElement.getBoundingClientRect().width;
    const resizeBoxLeft = component.resizeBoxElement.getBoundingClientRect().left;
    const width = dragHandleLeft - resizeBoxLeft + dragHandleWidth;
    component.collapsed = false;
    component.resize();
    let expectedWidth = Number(component.resizeBoxElement.style.width.slice(0, -2));
    expect(`${expectedWidth.toFixed(0)}px`).toEqual(`${width.toFixed(0)}px`);
    expect(setHandleTransformSpy).toHaveBeenCalled();
    expect(component.collapsed).toEqual(false);
  });

  it('it should toggle the collapsed state when resizing if already collapsed', () => {
    component.collapsed = true;
    component.resize();
    expect(component.collapsed).toEqual(false);
  });
});
