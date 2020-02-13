import { getLayerControl, getTimelineControl } from '@app/core/types/Packet';

describe('Packet', () => {

    let layerControl = null;
    let timelineControl = null;
    let packet = null;

    beforeEach(() => {
        layerControl = {
            showAsLayer: true,
            layerName: 'test_layer',
            zoomToChild: true,
            zoomToChildId: 'test_child_id',
            zoomStart: new Date('2019-01-01T00:00:00')
        };

        timelineControl = {
            id: 'test_timeline_id',
            content: 'test_content',
            start: new Date('2019-01-01T00:00:00'),
            end: new Date('2019-01-02T00:00:00'),
            style: '',
            group: ''
        };

        packet = {
            id: 'test_packet_id',
            parent: 'test_parent',
            name: 'test_name',
            availability: `${(new Date('2019-01-01T00:00:00')).toString()}/${(new Date('2019-01-02T00:00:00')).toString()}`,
            delete: false,
            layerControl,
            timelineControl
        };
    });

    it('should return the correct LayerControl by default', () => {
        const layer = getLayerControl(packet);
        expect(layer).toEqual(layerControl);
    });

    it('should return the correct LayerControl when packet.layerControl is null', () => {
        packet.layerControl = null;
        const layer = getLayerControl(packet);
        expect(layer).toEqual({
            showAsLayer: true,
            layerName: 'test_name',
            zoomToChild: false,
            zoomStart: new Date('2019-01-01T00:00:00')
        });
    });

    it('should set showAsLayer properly', () => {
        packet.layerControl.showAsLayer = null;
        const layer = getLayerControl(packet);
        expect(layer).toEqual({
            showAsLayer: true,
            layerName: 'test_layer',
            zoomToChild: true,
            zoomToChildId: 'test_child_id',
            zoomStart: new Date('2019-01-01T00:00:00')
        });
    });

    it('should set layerName properly', () => {
        packet.layerControl.layerName = null;
        const layer = getLayerControl(packet);
        expect(layer).toEqual({
            showAsLayer: true,
            layerName: 'test_name',
            zoomToChild: true,
            zoomToChildId: 'test_child_id',
            zoomStart: new Date('2019-01-01T00:00:00')
        });
    });

    it('should set zoomToChild properly', () => {
        packet.layerControl.zoomToChild = null;
        const layer = getLayerControl(packet);
        expect(layer).toEqual({
            showAsLayer: true,
            layerName: 'test_layer',
            zoomToChild: true,
            zoomToChildId: 'test_child_id',
            zoomStart: new Date('2019-01-01T00:00:00')
        });
    });

    it('should set availability properly', () => {
        packet.layerControl.zoomStart = null;
        packet.timelineControl = null;
        const layer = getLayerControl(packet);
        expect(layer).toEqual({
            showAsLayer: true,
            layerName: 'test_layer',
            zoomToChild: true,
            zoomToChildId: 'test_child_id',
            zoomStart: new Date('2019-01-01T00:00:00')
        });
    });

    it('should get undefined from getTimelineControl if packet.timelineControl is null', () => {
        packet.timelineControl = null;
        const timeline = getTimelineControl(packet);
        expect(timeline).toBeUndefined();
    });

    it('should get null from getTimelineControl if timelineControl.start and packet.availability are', () => {
        packet.timelineControl.start = null;
        packet.availability = null;
        const timeline = getTimelineControl(packet);
        expect(timeline).toBeNull();
    });

    it('should set timelineControl start/end dates if they\'re null', () => {
        packet.timelineControl.start = null;
        const timeline = getTimelineControl(packet);
        expect(timeline.start).toEqual(new Date('2019-01-01T00:00:00'));
        expect(timeline.end).toEqual(new Date('2019-01-02T00:00:00'));
    });

    it('should set timelineControl.id if it\'s null', () => {
        packet.timelineControl.id = null;
        const timeline = getTimelineControl(packet);
        expect(timeline.id).toEqual('test_packet_id');
    });

    it('should set timelineControl.id if it\'s null and packet.id is null', () => {
        packet.timelineControl.id = null;
        packet.id = null;
        const timeline = getTimelineControl(packet);
        expect(timeline.id).toMatch(/[a-z0-9]{8}(-[a-z0-9]{4}){3}-[a-z0-9]{12}/);
    });

    it('should set timelineControl content if it\'s null', () => {
        packet.timelineControl.content = null;
        const timeline = getTimelineControl(packet);
        expect(timeline.content).toEqual('test_name');
    });

    it('should set timelineControl content if it and packet.name are null', () => {
        packet.timelineControl.content = null;
        packet.name = null;
        const timeline = getTimelineControl(packet);
        expect(timeline.content).toEqual('test_packet_id');
    });

    it('should set timelineControl.style when it\'s null', () => {
        packet.timelineControl.style = null;
        const timeline = getTimelineControl(packet);
        let expectedStyle = 'height: 18px; background-color: rgba(64, 191, 96, 1); z-index: 2; ';
        expectedStyle += 'border-style: solid; border-color: green; border-width: 2px; text-align: center;';
        expect(timeline.style).toEqual(expectedStyle);
    });

    it('should set timelineControl.group when it\'s null', () => {
        packet.timelineControl.group = null;
        const timeline = getTimelineControl(packet);
        expect(timeline.group).toEqual('Data');
    });
});