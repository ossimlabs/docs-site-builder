import { SelectionModel } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { AfterViewInit, Component } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDatepickerInputEvent } from '@angular/material';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TimeChangeService } from '@app/core';
import { DataService, PacketEvent, SetVisibleEvent } from '@app/core/services/data.service';
import { getSessionNamesMap } from '@app/core/session-utils';
import { getLayerControl, Packet } from '@app/core/types/Packet';

export class PacketFlatNode {

  private _partialSelected = false;

  constructor(
    public id: string,
    public parentId: string,
    public name: string,
    public level: number,
    public children: string[],
    private _selected: boolean,
    public sessionId: string,
    public packet: Packet) { }

  get selected() { return this._selected || this._partialSelected; }
  set selected(value) { this._selected = value; }
  get partialSelected() { return this._partialSelected; }
  get deselected() { return !this.selected; }

  select() {
    this._partialSelected = false;
    this._selected = true;
  }

  partialSelect() {
    this._partialSelected = true;
    this._selected = false;
  }

  deselect() {
    this._partialSelected = false;
    this._selected = false;
  }

  toggle() {
    if (this.selected) {
      this.deselect();
    } else {
      this.select();
    }
  }

  expandable() {
    return this.children.length > 0;
  }
}

@Component({
  selector: 'app-layer-tree',
  templateUrl: './layer-tree.component.html',
  styleUrls: ['./layer-tree.component.css']
})
export class LayerTreeComponent implements AfterViewInit {

  sessionPackets: Map<string, PacketFlatNode> = new Map();
  packetLevels: Record<string, Record<string, PacketFlatNode>> = {};

  treeControl = new FlatTreeControl<PacketFlatNode>(
    node => node.level, node => node.expandable());

  treeFlattener = new MatTreeFlattener(
    (node: PacketFlatNode, level: number) => {
      return node;
    },
    (node: PacketFlatNode) => node.level,
    (node: PacketFlatNode) => node.expandable(),
    (node: PacketFlatNode) => this.getChildren(node));

  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

  checklistSelection = new SelectionModel<string>(true);

  jumpToDateControl = new FormControl(new Date());

  get dataService() { return this._dataService; }
  get timeChangeService() { return this._timeChangeService; }

  constructor(
    private _dataService: DataService,
    private _timeChangeService: TimeChangeService,
    private router: Router,
    private activatedRoute: ActivatedRoute) {

    this.dataSource.data = [];

    const makeNodes = (sessionId: string, packets: Packet[]) => {
      if (!packets) {
        this.packetLevels[sessionId] = {};
        const sessionPacket = this.sessionPackets.get(sessionId);
        sessionPacket.children = [];
        this.packetLevels[sessionId][sessionId] = sessionPacket;
        return;
      }

      const packetMap: Record<string, Packet> = {};
      for (const packet of packets) {
        if (!packetMap[packet.id]) {
          packetMap[packet.id] = packet;
        }
      }

      const packetFlatener = (packet: Packet) => {
        if (!packet.delete) {
          const layerControl = getLayerControl(packet);
          if (!this.packetLevels[sessionId][packet.id] && layerControl.showAsLayer) {
            const parentId = packetMap[packet.id].parent;
            if (!parentId) {
              this.packetLevels[sessionId][packet.id] = new PacketFlatNode(packet.id, sessionId, layerControl.layerName, 1, [], this.packetLevels[sessionId][sessionId].selected, sessionId, packet);
              this.packetLevels[sessionId][sessionId]['children'].push(packet.id);
            } else {
              if (!this.packetLevels[sessionId][parentId]) {
                packetFlatener(packetMap[parentId]);
              }
              const parent = this.packetLevels[sessionId][parentId];
              this.packetLevels[sessionId][packet.id] = new PacketFlatNode(packet.id, parentId, layerControl.layerName, parent.level + 1, [], parent.selected, sessionId, packet);
              this.packetLevels[sessionId][parentId]['children'].push(packet.id);
            }
          }
        } else {
          //Deleting a packet
          const flatNode = this.packetLevels[sessionId][packet.id];

          //only need to do something if this packet is in the tree
          if (flatNode) {

            const parent: PacketFlatNode = this.packetLevels[sessionId][flatNode.parentId];

            //Remove this packet from its parent
            parent.children.splice(parent.children.indexOf(flatNode.id), 1);

            const packetsToDelete = new Set();
            packetsToDelete.add(flatNode.id);

            packetsToDelete.forEach((nodeToDelete: string) => {
              if (this.packetLevels[sessionId][nodeToDelete]) {
                //Add the packets children as nodes that need to be deleted
                this.packetLevels[sessionId][nodeToDelete].children.forEach((addNode) => { packetsToDelete.add(addNode) });
                this.packetLevels[sessionId][nodeToDelete] = null
              }
            });
          }
        }
      };

      for (const packetId of Object.keys(packetMap)) {
        packetFlatener(packetMap[packetId]);
      }
    };

    this.dataService.getPacketDataObservable().subscribe((event: PacketEvent) => {
      makeNodes(event.session, event.packets);

      this.dataSource.data = Array.from(this.sessionPackets.values());
    });

    this.timeChangeService.getMapTimeChangedObservable().subscribe(event => {
      const utcDate = new Date(event.time);
      utcDate.setHours(utcDate.getHours() + (utcDate.getTimezoneOffset() / 60.0))
      this.jumpToDateControl.setValue(utcDate);
    });
  }

  ngAfterViewInit() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const sessionMap: Map<string, string> = getSessionNamesMap(this.activatedRoute.snapshot.queryParamMap);

        for (const sessionId of Array.from(sessionMap.keys())) {
          this.packetLevels[sessionId] = {};
          const node = new PacketFlatNode(sessionId, null, sessionMap.get(sessionId), 0, [], true, sessionId, null);
          this.sessionPackets.set(sessionId, node);
          this.packetLevels[sessionId][sessionId] = node;
        }

        this.dataSource.data = Array.from(this.sessionPackets.values());
      }
    });
  }

  getLevel = (node: PacketFlatNode) => node.level;

  hasChild = (_: number, node: PacketFlatNode) => node.expandable();

  getChildren = (node: PacketFlatNode) => node.children.map(child => this.packetLevels[node.sessionId][child]);

  itemSelectionToggle(node: PacketFlatNode): void {
    node.toggle();
    let updatedIds = this.updateChildren(node.sessionId, node.selected, node.children);
    updatedIds.push(node.id);
    const parentsIds = this.updateParent(node.sessionId, node.parentId);
    if (node.selected) {
      // If we turn on a child, make sure we turn on all it's parents.
      // If we turn off a child, don't turn off parents.
      updatedIds = updatedIds.concat(parentsIds);
    }
    this.dataService.setEntitiesVisible(new SetVisibleEvent(node.selected, updatedIds));
  }

  updateChildren(sessionId: string, select: boolean, children: string[]) {
    let childrenUpdated = [...children];

    for (const childId of children) {
      const child = this.packetLevels[sessionId][childId];
      select ? child.select() : child.deselect();
      childrenUpdated = childrenUpdated.concat(this.updateChildren(sessionId, select, child.children));
    }

    return childrenUpdated;
  }

  updateParent(sessionId: string, parentId: string): string[] {
    if (parentId == null) {
      return [];
    }

    const node = this.packetLevels[sessionId][parentId];

    const children = this.getChildren(node);
    if (children.every(childNode => childNode.selected)) {
      node.select();
    } else if (children.some(childNode => childNode.selected)) {
      node.partialSelect();
    }

    const parentIds = this.updateParent(sessionId, node.parentId)
    parentIds.push(parentId);
    return parentIds;
  }

  itemClicked(node: PacketFlatNode) {
    if (!node.selected) {
      this.itemSelectionToggle(node);
    }

    const zoomPacket = this.getPacketForZoom(node);
    this.zoomToPacket(zoomPacket);
  }

  getPacketForZoom(node: PacketFlatNode): Packet {
    if (!node.packet) {
      return null;
    }

    const layerControl = getLayerControl(node.packet);

    let packetToZoomTo;
    if (layerControl.zoomToChild) {
      const childId = layerControl.zoomToChildId;

      const childPacket = this.packetLevels[node.sessionId][childId];
      if (childPacket) {
        packetToZoomTo = childPacket.packet;
      }

      if (!packetToZoomTo) {
        packetToZoomTo = node.packet;
      }

    } else {
      packetToZoomTo = node.packet;
    }

    return packetToZoomTo;
  }

  zoomToPacket(packet: Packet) {
    if (packet) {
      const layerControl = getLayerControl(packet);

      const zoomTime = layerControl.zoomStart ? layerControl.zoomStart : null;
      this.dataService.sendSelection(packet.id, layerControl.zoomStart);

      if (zoomTime) {
        this.timeChangeService.sendMapTimeChanged(layerControl.zoomStart, true);
      }
    }
  }

  jumpToDate(event: MatDatepickerInputEvent<Date>) {
    const date = event.value;
    date.setUTCHours(0, 0, 0, 0);
    this.timeChangeService.sendTimelineTimeChanged(date);
    this.timeChangeService.sendMapTimeChanged(date, true);
  }
}
