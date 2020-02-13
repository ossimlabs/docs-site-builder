export class LayersConfig {

    static baseLayers: BaseLayer[] = [
        {
            name: 'Blue Marble',
            iconUrl: 'assets/icons/globe.png',
            url: 'http://10.0.0.191:8080/geoserver/cite/wms',
            layers: 'cite:bmng'
        }
    ];
}

interface BaseLayer {
    name: string,
    iconUrl: string,
    url: string,
    layers: string,
}
