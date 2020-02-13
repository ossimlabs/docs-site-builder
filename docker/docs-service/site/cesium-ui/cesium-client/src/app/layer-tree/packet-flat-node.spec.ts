import { PacketFlatNode } from './layer-tree.component';

describe('PacketFlatNode', () => {

    let packetFlatNode = null;

    beforeEach(() => {
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

    it('should select nodes properly', () => {
        packetFlatNode.select();
        expect(packetFlatNode.selected).toBeTruthy();
        expect(packetFlatNode.partialSelected).toBeFalsy();
    });

    it('should partally select nodes properly', () => {
        packetFlatNode.partialSelect();
        expect(packetFlatNode.selected).toBeTruthy();
        expect(packetFlatNode.partialSelected).toBeTruthy();
    });

    it('should deselect nodes properly', () => {
        packetFlatNode.deselect();
        expect(packetFlatNode.selected).toBeFalsy();
        expect(packetFlatNode.partialSelected).toBeFalsy();
        expect(packetFlatNode.deselected).toBeTruthy();
    });

    it('should toggle nodes properly', () => {
        packetFlatNode.toggle();
        expect(packetFlatNode.selected).toBeFalsy();
        expect(packetFlatNode.partialSelected).toBeFalsy();
        packetFlatNode.toggle();
        expect(packetFlatNode.selected).toBeTruthy();
        expect(packetFlatNode.partialSelected).toBeFalsy();
    });

    it('should determine its expandability properly', () => {
        expect(packetFlatNode.expandable()).toBeTruthy();
        packetFlatNode.children = [];
        expect(packetFlatNode.expandable()).toBeFalsy();
    });
});