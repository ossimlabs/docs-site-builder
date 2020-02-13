import { TestBed } from '@angular/core/testing';

import { TimeChangeService, ChangeTimelineTimeEvent } from './time-change.service';

describe('TimeChangeService', () => {
  let service: TimeChangeService = null;

  beforeEach(() => {
    TestBed.configureTestingModule({});

    service = TestBed.get(TimeChangeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // it('should send a new timeline selection', () => {
  //   service.getTimelineSelectionChanged().subscribe(item => expect(item).toEqual(1));
  //   service.sendTimelineSelectionChanged(1);
  // });

  it('should send a new timeline time', () => {
    const date = new Date('1987-09-29');
    service.getTimelineTimeChangedObservable().subscribe(time => expect(time).toEqual(date));
    service.sendTimelineTimeChanged(date);
    expect(service.currentSimTime).toEqual(date);
  });

  it('should send a new map time', () => {
    const date = new Date('1987-09-29');
    service.getMapTimeChangedObservable().subscribe(event => expect(event).toEqual(new ChangeTimelineTimeEvent(new Date('1987-09-29'), true)));
    service.sendMapTimeChanged(date, true);
    expect(service.currentSimTime).toEqual(date);
  });

  // it('should send zoom times to the timeline', () => {
  //   const start = new Date('1987-09-29');
  //   const end = new Date('1987-09-30');
  //   service.getZoomTimelineObervable().subscribe(timeRange => expect(timeRange).toEqual([start, end]));
  //   service.sendZoomTimeline([start, end]);
  // });

  it('should focus the timelime', () => {
    const date = new Date('1987-09-29');
    service.getTimelineTimeChangedObservable().subscribe(focusDate => expect(focusDate).toEqual(date));
    service.sendTimelineTimeChanged(date);
  });
});
