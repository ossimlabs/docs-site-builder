import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export class ChangeTimelineTimeEvent {
  constructor(readonly time: Date, readonly centerTime: Boolean) { }
}

@Injectable({
  providedIn: 'root'
})
export class TimeChangeService {

  private timelineTimeChangedSource = new Subject<Date>();
  private mapTimeChangedSource = new Subject<ChangeTimelineTimeEvent>();

  public currentSimTime: Date;

  constructor() { }

  public sendTimelineTimeChanged(time: Date) {
    if (time !== this.currentSimTime) {

      this.currentSimTime = time;
      this.timelineTimeChangedSource.next(time);

    }
  }

  public getTimelineTimeChangedObservable() {
    return this.timelineTimeChangedSource.asObservable();
  }

  public sendMapTimeChanged(time: Date, centerTime: Boolean) {
    if (time !== this.currentSimTime || centerTime) {

      this.currentSimTime = time;
      this.mapTimeChangedSource.next(new ChangeTimelineTimeEvent(time, centerTime));

    }
  }

  public getMapTimeChangedObservable() {
    return this.mapTimeChangedSource.asObservable();
  }
}
