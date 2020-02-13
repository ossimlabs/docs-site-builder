import { v4 as uuid } from 'uuid';

export class Packet {
    id: string;
    parent: string;
    name: string;
    availability: string;
    delete: boolean;
    layerControl: LayerControl;
    timelineControl: TimelineControl;
}

export class LayerControl {
    showAsLayer: boolean;
    layerName: string;
    zoomToChild: boolean;
    zoomToChildId?: string;
    zoomStart?: Date;
}

export function getLayerControl(packet: Packet): LayerControl {

    let layerControl;
    if (packet.layerControl) {
        layerControl = packet.layerControl
        if (layerControl.showAsLayer == null) {
            layerControl.showAsLayer = true;
        }
        if (layerControl.layerName == null) {
            layerControl.layerName = packet.name
        }

        if (layerControl.zoomToChild == null) {
            layerControl.zoomToChild = layerControl.zoomToChildId != null
        }
    } else {
        layerControl = {
            showAsLayer: true,
            layerName: packet.name,
            zoomToChild: false
        }
    }

    if (layerControl.zoomStart == null) {
        const timelineControl = getTimelineControl(packet)
        if (timelineControl) {
            layerControl.zoomStart = timelineControl.start;
        } else if (packet.availability) {
            layerControl.zoomStart = new Date(packet.availability.split('/')[0]);
        }
    } else {
        layerControl.zoomStart = new Date(layerControl.zoomStart);
    }

    return layerControl;
}

export class TimelineControl {
    id: string;
    content: string;
    start: Date;
    end: Date;
    style: string;
    group: string;
}

export function getTimelineControl(packet: Packet): TimelineControl {
    if (packet.timelineControl) {
        const timelineControl = packet.timelineControl;

        if (timelineControl.start == null && packet.availability == null) {
            return null;
        }

        if (timelineControl.start == null) {
            timelineControl.start = new Date(packet.availability.split('/')[0]);
            timelineControl.end = new Date(packet.availability.split('/')[1]);
        } else {
            timelineControl.start = new Date(timelineControl.start);
            timelineControl.end = new Date(timelineControl.end);
        }

        const id = packet.id != null ? packet.id : uuid();

        if (timelineControl.id == null) {
            timelineControl.id = id;
        }

        if (timelineControl.content == null) {
            if (packet.name != null) {
                timelineControl.content = packet.name;
            } else {
                timelineControl.content = id;
            }
        }

        if (timelineControl.style == null) {
            timelineControl.style = 'height: 18px; background-color: rgba(64, 191, 96, 1); z-index: 2; ' +
                'border-style: solid; border-color: green; border-width: 2px; text-align: center;'
        }

        if (timelineControl.group == null) {
            timelineControl.group = "Data";
        }

        return timelineControl;
    } else {
        null;
    }
}