import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LayerTreeComponent, PacketFlatNode } from './layer-tree.component';
import { MatTreeModule, MatIconModule, MatCheckboxModule, MatDatepickerModule, MatFormFieldModule, MatNativeDateModule, MatInputModule } from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';
import { SelectionEvent } from '@app/core/services/data.service';
import { ChangeTimelineTimeEvent } from '@app/core/services/time-change.service';
import { compileNgModule } from '@angular/compiler';

describe('LayerTreeComponent', () => {
  let component: LayerTreeComponent;
  let fixture: ComponentFixture<LayerTreeComponent>;
  let packetFlatNode: PacketFlatNode = null;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LayerTreeComponent ],
      imports: [
        BrowserAnimationsModule,
        HttpClientModule,
        MatCheckboxModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatNativeDateModule,
        MatTreeModule,
        RouterTestingModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LayerTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const layerControl = {
        showAsLayer: true,
        layerName: 'test_layer',
        zoomToChild: true,
        zoomToChildId: 'test_child_id',
        zoomStart: new Date('2019-01-01T00:00:00')
    };

    const timelineControl = {
        id: 'test_timeline_id',
        content: 'test_content',
        start: new Date('2019-01-01T00:00:00'),
        end: new Date('2019-01-02T00:00:00'),
        style: '',
        group: ''
    };

    const packet = {
        id: 'test_packet_id',
        parent: 'test_parent',
        name: 'test_name',
        availability: `${(new Date('2019-01-01T00:00:00')).toString()}/${(new Date('2019-01-02T00:00:00')).toString()}`,
        delete: false,
        layerControl,
        timelineControl
    };

    packetFlatNode = new PacketFlatNode(
        'id',
        'parentId',
        'name',
        1,
        ['child1', 'child2'],
        true,
        'sessionID',
        packet
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should zoom to the provided packet', () => {
    const expectedSelection = new SelectionEvent('test_packet_id', new Date('2019-01-01T00:00:00'));
    const expectedTime = new ChangeTimelineTimeEvent(new Date('2019-01-01T00:00:00'), true);
    component.dataService.getSelectionObservable().subscribe(selection => expect(selection).toEqual(expectedSelection));
    component.timeChangeService.getMapTimeChangedObservable().subscribe(time => expect(time).toEqual(expectedTime));
    component.zoomToPacket(packetFlatNode.packet);
  });

  it('should zoom without a zoomStart', () => {
    packetFlatNode.packet.layerControl.zoomStart = null;
    packetFlatNode.packet.timelineControl.start = null;
    packetFlatNode.packet.availability = null;
    const expectedSelection = new SelectionEvent('test_packet_id', null);
    component.dataService.getSelectionObservable().subscribe(selection => expect(selection).toEqual(expectedSelection));
    component.timeChangeService.getMapTimeChangedObservable().subscribe(time => fail());
    component.zoomToPacket(packetFlatNode.packet);
  });

  it('should not zoom to a null packet', () => {
    component.dataService.getSelectionObservable().subscribe(selection => fail());
    component.zoomToPacket(null);

    // Make this test pass by default so the test runner doesn't complain about
    // it not having expecations
    expect(true).toBeTruthy();
  });

  it('should return null if no packet exists', () => {
    packetFlatNode.packet = null;

    const returnedPacket = component.getPacketForZoom(packetFlatNode);
    expect(returnedPacket).toBeNull();
  });

  it('should get packet for zoom', () => {
    component.packetLevels['sessionID'] = {};
    component.packetLevels['sessionID']['test_child_id'] = packetFlatNode;
    const returnedPacket = component.getPacketForZoom(packetFlatNode);
    expect(returnedPacket).toEqual(packetFlatNode.packet);
  });

  it('should handle zoomToChild being false', () => {
    packetFlatNode.packet.layerControl.zoomToChild = false;
    const returnedPacket = component.getPacketForZoom(packetFlatNode);
    expect(returnedPacket).toEqual(packetFlatNode.packet);
  });

  it('should handle not being able to find a childPacket', () => {
    component.packetLevels['sessionID'] = {};
    component.packetLevels['sessionID']['test_child_id'] = null;
    const returnedPacket = component.getPacketForZoom(packetFlatNode);
    expect(returnedPacket).toEqual(packetFlatNode.packet);
  });

  it('should handle clicking of an item when it\'s already selected', () => {
    const getPacketForZoomSpy = spyOn(component, 'getPacketForZoom');
    const zoomToPacketSpy = spyOn(component, 'zoomToPacket');
    component.itemClicked(packetFlatNode);
    expect(getPacketForZoomSpy).toHaveBeenCalled();
    expect(zoomToPacketSpy).toHaveBeenCalled();
  });

  it('should handle clicking of an item when it\'s not already selected', () => {
    const itemSelectionToggleSpy = spyOn(component, 'itemSelectionToggle');
    const getPacketForZoomSpy = spyOn(component, 'getPacketForZoom');
    const zoomToPacketSpy = spyOn(component, 'zoomToPacket');
    packetFlatNode.selected = false;
    component.itemClicked(packetFlatNode);
    expect(itemSelectionToggleSpy).toHaveBeenCalled();
    expect(getPacketForZoomSpy).toHaveBeenCalled();
    expect(zoomToPacketSpy).toHaveBeenCalled();
  });

  it('should update parent when all children are selected', () => {
    const child1PacketNode = packetFlatNode;
    child1PacketNode.packet.id = 'child1';
    child1PacketNode.children = [];
    const child2PacketNode = packetFlatNode;
    child2PacketNode.packet.id = 'child2';
    child2PacketNode.children = [];
    const parentPacketNode = packetFlatNode;
    parentPacketNode.packet.id = 'parentId';
    parentPacketNode.parentId = null;
    parentPacketNode.children = ['child1', 'child2'];
    component.packetLevels['sessionID'] = {};
    component.packetLevels['sessionID']['test_child_id'] = packetFlatNode;
    component.packetLevels['sessionID']['child1'] = child1PacketNode;
    component.packetLevels['sessionID']['child2'] = child2PacketNode;
    component.packetLevels['sessionID']['parentId'] = parentPacketNode;
    const parents = component.updateParent('sessionID', 'test_child_id');
    expect(parents).toEqual(['test_child_id']);
    expect(packetFlatNode.selected).toBeTruthy();
  });

  it('should get the packet\'s level', () => {
    const level = component.getLevel(packetFlatNode);
    expect(level).toEqual(1);
  });

  it('should determine if a packet has a child', () => {
    const child = component.hasChild(0, packetFlatNode);
    expect(child).toBeTruthy();
  });

  it('should get a packet\'s children', () => {
    const child1PacketNode = packetFlatNode;
    child1PacketNode.packet.id = 'child1';
    child1PacketNode.children = [];
    const child2PacketNode = packetFlatNode;
    child2PacketNode.packet.id = 'child2';
    child2PacketNode.children = [];
    const parentPacketNode = packetFlatNode;
    parentPacketNode.packet.id = 'parentId';
    parentPacketNode.parentId = null;
    parentPacketNode.children = ['child1', 'child2'];
    component.packetLevels['sessionID'] = {};
    component.packetLevels['sessionID']['test_child_id'] = packetFlatNode;
    component.packetLevels['sessionID']['child1'] = child1PacketNode;
    component.packetLevels['sessionID']['child2'] = child2PacketNode;
    component.packetLevels['sessionID']['parentId'] = parentPacketNode;
    const children = component.getChildren(packetFlatNode);
    expect(children.length).toEqual(2);
  });
});
