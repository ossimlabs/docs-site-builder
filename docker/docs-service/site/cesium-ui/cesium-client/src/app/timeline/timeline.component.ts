import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { TimeChangeService } from '@app/core';
import { DataService, PacketEvent } from '@app/core/services/data.service';
import { DataItem, DataSet, Timeline, DataGroup } from 'vis';
import { getTimelineControl, TimelineControl } from '@app/core/types/Packet';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { getSessionNamesMap } from '@app/core/session-utils';

declare var vis: any;

@Component({
  selector: 'app-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.css']
})
export class TimelineComponent implements AfterViewInit {

  @ViewChild('timeline', { static: false }) timelineContainer: ElementRef;

  private _groupStyle = 'width: 167px; height: 15px;';

  timeline: Timeline;
  items: DataSet<DataItem>;
  groups: DataSet<DataGroup>;
  sessionItems: Map<string, Set<string>> = new Map();
  options: {};

  get timeService() { return this._timeService; }
  get dataService() { return this._dataService; }
  get router() { return this._router; }
  get groupStyle() { return this._groupStyle; }

  constructor(
    private _timeService: TimeChangeService,
    private _dataService: DataService,
    private _router: Router,
    private activatedRoute: ActivatedRoute) {
    this.options = {
      editable: false,
      showTooltips: true,
      tooltip: {
        followMouse: true,
        overflowMethod: 'cap'
      },
      margin: {
        item: 20,
        axis: 40
      },
      showCurrentTime: false,
      start: Date.now(),
      stack: false,
      moment: function (date) {
        return vis.moment(date).utc();
      },
      orientation: 'top',
      zoomKey: 'ctrlKey',
      height: '100%',
      verticalScroll: true
    };

    this.items = new vis.DataSet([
    ]);

    this.groups = new vis.DataSet([]);
  }

  ngAfterViewInit() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const sessionMap: Map<string, string> = getSessionNamesMap(this.activatedRoute.snapshot.queryParamMap);

        for (const sessionId of Array.from(sessionMap.keys())) {
          this.groups.add({
            id: sessionId,
            content: sessionMap.get(sessionId),
            style: this.groupStyle,
            nestedGroups: []
          });

          this.sessionItems.set(sessionId, new Set());
        }
      }
    });

    this.timeline = new vis.Timeline(this.timelineContainer.nativeElement, this.items, this.groups, this.options);

    // Add a custom time bar for 'sim' time
    this.timeline.addCustomTime(Date.now(), 'cesium');

    this.timeline.on('timechange', this.onTimeChangeEvent.bind(this));
    this.timeline.on('rangechange', this.onTimeChangeEvent.bind(this));

    this.timeline.on('select', this.onSelectChangeEvent.bind(this));

    this.timeService.getMapTimeChangedObservable().subscribe(timeEvent => {
      this.timeline.setCustomTime(timeEvent.time, 'cesium');
      if (timeEvent.centerTime) {
        this.timeline.moveTo(timeEvent.time);
      }
    });

    this.dataService.getPacketDataObservable().subscribe((event: PacketEvent) => {
      if (!event.packets) {
        this.groups.remove(this.groups.get(event.session).nestedGroups)
        this.groups.update({
          id: event.session,
          nestedGroups: []
        })

        this.items.remove(Array.of(this.sessionItems.get(event.session)));
        this.sessionItems.set(event.session, new Set());

        return;
      }

      let dataStart: Date;
      let dataEnd: Date;

      const timelineEmpty: boolean = this.items.length === 0

      const groupsInPackets = new Set<string>();
      const groupsToCheckForDelete = new Set<string>();
      for (const packet of event.packets) {
        const timelineControl = getTimelineControl(packet);
        if (packet.delete && packet.id) {

          const packetToDelete: TimelineControl = this.items.get(packet.id)
          if (packetToDelete != null) {
            groupsToCheckForDelete.add(packetToDelete.group)
            this.items.remove(packet.id)
          }
        } else if (timelineControl) {
          const packetStart = timelineControl.start;
          const packetEnd = timelineControl.end;

          if (dataStart == null || packetStart.valueOf() <= dataStart.valueOf()) {
            dataStart = packetStart;
          }

          if (dataEnd == null || (packetEnd != null && packetEnd.valueOf() >= dataEnd.valueOf())) {
            dataEnd = packetEnd;
          }

          groupsInPackets.add(timelineControl.group);

          timelineControl.group = event.session + timelineControl.group;

          this.items.update(timelineControl);
        }
      }

      groupsInPackets.forEach((group) => {
        const nestedGroups: Array<string> = this.groups.get(event.session).nestedGroups;
        if (!nestedGroups.includes(group)) {
          nestedGroups.push(event.session + group);
          this.groups.update(
            {
              id: event.session,
              nestedGroups: nestedGroups
            }
          );
        }
        this.groups.update({
          id: event.session + group,
          content: group,
          style: this.groupStyle
        });
      });
      groupsToCheckForDelete.forEach((group) => {
        //Check if items left on the timeline are in this group
        const itemsInGroup = this.items.getIds({
          filter: (item) => {
            item.group == group
          }
        }).length;

        if (itemsInGroup == 0) {
          //Remove the group from nested groups
          const nestedGroups: Array<string> = this.groups.get(event.session).nestedGroups;
          nestedGroups.slice(nestedGroups.indexOf(group), 1)
          this.groups.update(
            {
              id: event.session,
              nestedGroups: nestedGroups
            }
          );

          //Remove the group
          this.groups.remove(group);
        }
      });

      //Timeline was empty, but now its not, sets its bounds and the custom time
      if (timelineEmpty && this.items.length > 0) {
        this.timeline.setWindow(dataStart, dataEnd);
        this.timeline.setCustomTime(dataStart, 'cesium');
      }
    });
  }

  onTimeChangeEvent(event) {
    this.timeService.sendTimelineTimeChanged(this.timeline.getCustomTime('cesium'));
  }

  onSelectChangeEvent(event) {
    const selectedId = event.items[0];

    if (selectedId != null) {
      const item = this.items.get(selectedId);
      this.dataService.sendSelection(item.id, item.start);
    }
  }
}
