import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { DataSet } from 'vis';

import { TimelineComponent } from './timeline.component';
import { RouterTestingModule } from '@angular/router/testing';
import { NavigationEnd } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

describe('TimelineComponent', () => {
  let component: TimelineComponent;
  let fixture: ComponentFixture<TimelineComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TimelineComponent ],
      imports: [
        HttpClientModule,
        RouterTestingModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // it('should handle zoom timeline changes', () => {
  //   const start = new Date('1987-09-29');
  //   const end = new Date('1987-09-30');
  //   const setWindowSpy = spyOn(component.timeline, 'setWindow');
  //   component.timeService.sendZoomTimeline([start, end]);
  //   expect(setWindowSpy).toHaveBeenCalled();
  //   expect(component.timeline.getWindow().start).toEqual(jasmine.any(Date));
  //   expect(component.timeline.getWindow().end).toEqual(jasmine.any(Date));
  // });

  // it('should handle zoom timeline changes with null ranges', () => {
  //   component.timeService.getTimelineSelectionChanged().subscribe(item => expect(item).toEqual({ start: null, end: null }));
  //   component.timeService.sendZoomTimeline([null, null]);
  // });

  // it('should handle timeline focus events', () => {
  //   const customDate = new Date('1987-09-29');
  //   const moveToSpy = spyOn(component.timeline, 'moveTo');
  //   component.timeService.getTimelineSelectionChanged().subscribe(item => expect(item).toEqual({ start: null, end: null }));
  //   component.timeService.getTimelineTimeChanged().subscribe(date => expect(date).toEqual(customDate));
  //   component.timeService.getTimelineSelectionChanged().subscribe(item => expect(item).toEqual({ start: null, end: null }));
  //   component.timeService.focusTimeline(customDate);
  //   expect(moveToSpy).toHaveBeenCalled();
  // });

  it('should send new time to Cesium when the timeline is changed', () => {
    const customDate = new Date('1987-09-29');
    component.timeline.setCustomTime(customDate, 'cesium');
    component.timeService.getTimelineTimeChangedObservable().subscribe(date => expect(date).toEqual(customDate));
    component.onTimeChangeEvent(null);
  });

  // it('should handle selection change events', () => {
  //   const event = { items: [1], event: null };
  //   component.items = new DataSet([
  //     {id: 1, content: 'read-only item', start: '1987-09-29', editable: false},
  //     {id: 2, content: 'editable item', start: '1987-09-30'}
  //   ]);
  //   const itemsGetSpy = spyOn(component.items, 'get');
  //   component.onSelectChangeEvent(event);
  //   expect(itemsGetSpy).toHaveBeenCalled();
  // });

  it('should set custom time when map time changes', () => {
    const customTimeSpy = spyOn(component.timeline, 'setCustomTime');
    component.timeService.sendMapTimeChanged(new Date(), true);
    expect(customTimeSpy).toHaveBeenCalled();
  });

  it('should handle null packet data', () => {
    component.groups.add({
      id: 'sessionKey',
      content: 'sessionValue',
      style: component.groupStyle,
      nestedGroups: [{}, {}]
    });
    component.dataService.sendPacketData('sessionKey', null);
    expect(component.groups.get('sessionKey').nestedGroups.length).toEqual(0);
    expect(component.sessionItems.get('sessionKey')).toEqual(jasmine.any(Set));
  });

  it('should get session data from route', () => {
    expect(component.groups.length).toEqual(0);
    component.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        expect(component.groups.length).toEqual(1);
        expect(component.sessionItems.get('sessionKey')).toEqual(jasmine.any(Set));
      }
    });

    component.router.navigateByUrl('?session=sessionKey&sessionName=sessionValue');
  });
});
