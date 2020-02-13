import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CesiumMapComponent } from './cesium-map.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatIconModule } from '@angular/material';
import { AngularCesiumModule } from 'angular-cesium';
import { SetVisibleEvent } from '@app/core/services/data.service';
import { HttpClientModule } from '@angular/common/http';

describe('CesiumMapComponent', () => {
  let component: CesiumMapComponent;
  let fixture: ComponentFixture<CesiumMapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        CesiumMapComponent,
      ],
      imports: [
        AngularCesiumModule.forRoot(),
        DragDropModule,
        HttpClientModule,
        MatIconModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CesiumMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fly to entity', () => {
    const flyToSpy = spyOn(component.acMap.getCesiumViewer(), 'flyTo');
    component.dataService.sendSelection('id1', new Date('1987-09-29'));
    expect(flyToSpy).toHaveBeenCalled();
  });

  it('should handle new packet data', () => {
    component.dataService.sendPacketData('session1234', '{ "key": "value" }');
    expect(component.sessionMap['session1234']).toEqual(jasmine.any(Set));
  });

  // it('should handle timeline selection changes', () => {
  //   const start = new Date('1987-09-29');
  //   const end = new Date('1987-09-30');
  //   const item = { start, end };
  //   component.timeService.sendTimelineSelectionChanged(item);

  //   const viewer = component.acMap.getCesiumViewer();
  //   expect(viewer.clock.startTime).toEqual(Cesium.JulianDate.fromDate(start));
  //   expect(viewer.clock.multiplier).toEqual(180);
  //   expect(viewer.clock.stopTime).toEqual(Cesium.JulianDate.fromDate(end));
  // });

  // it('should handle null timeline change dates', () => {
  //   const item = { start: null, end: null };
  //   component.timeService.sendTimelineSelectionChanged(item);

  //   const viewer = component.acMap.getCesiumViewer();
  //   expect(viewer.clock.startTime).toBeNull();
  //   expect(viewer.clock.stopTime).toBeNull();
  // });

  // it('should handle timeline selection changes with IDs and start/stop times', () => {
  //   const start = new Date('1987-09-29');
  //   const end = new Date('1987-09-30');
  //   const item = { id: 'item_id', start, end };
  //   component.timeService.sendTimelineSelectionChanged(item);

  //   const viewer = component.acMap.getCesiumViewer();
  //   expect(viewer.clock.startTime).toEqual(Cesium.JulianDate.fromDate(start));
  //   expect(viewer.clock.multiplier).toEqual(180);
  // });

  // it('should handle timeline selection changes with IDs but without start/stop times', () => {
  //   const item = { id: 'item_id', start: null, end: null };
  //   component.timeService.sendTimelineSelectionChanged(item);

  //   const viewer = component.acMap.getCesiumViewer();
  //   expect(viewer.clock.startTime).toBeNull();
  //   expect(viewer.clock.multiplier).toEqual(180);
  // });

  it('should handle timeline time changes', () => {
    const date = new Date('1987-09-29');
    component.timeService.sendTimelineTimeChanged(date);

    const viewer = component.acMap.getCesiumViewer();
    expect(viewer.clock.currentTime).toEqual(Cesium.JulianDate.fromDate(date));
  });

  it('should detect mousemove events', () => {
    const mouseMoveSpy = spyOn(component, 'handleMouseMove');
    const canvas = document.getElementsByTagName('canvas')[0];
    canvas.dispatchEvent(new Event('mousemove'));
    expect(mouseMoveSpy).toHaveBeenCalled();
  })
});
