import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { TimeChangeService } from '@app/core';
import { DataService, PacketEvent } from '@app/core/services/data.service';
import { AcMapComponent, ViewerConfiguration } from 'angular-cesium';
import { LayersConfig } from '@app/cesium-map/layer';


@Component({
  selector: 'app-cesium-map',
  templateUrl: './cesium-map.component.html',
  styleUrls: ['./cesium-map.component.css'],
  providers: [ViewerConfiguration]
})
export class CesiumMapComponent implements AfterViewInit {

  @ViewChild(AcMapComponent, { static: false }) acMap;

  private doc = [{ 'id': 'document', 'name': 'Sim-Cesium document', 'version': '1.0' }];

  private _sessionMap: Record<string, Set<string>> = {};

  get dataService() { return this._dataService; }
  get timeService() { return this._timeService; }
  get sessionMap() { return this._sessionMap; }

  private baseLayerPicker: boolean = false;

  constructor(
    private viewerConf: ViewerConfiguration,
    private _timeService: TimeChangeService,
    private _dataService: DataService) {

    let imageryProvider;
    let imageryProviderViewModels = [];

    LayersConfig.baseLayers.forEach(layer => {
      imageryProviderViewModels.push(
        new Cesium.ProviderViewModel({
          name: layer.name,
          iconUrl: layer.iconUrl,
          tooltip: '',
          creationFunction: function() {
            return new Cesium.WebMapServiceImageryProvider({
              url: layer.url,
              layers: layer.layers,
              enablePickFeatures: false
            });
          }
        })
      );
    });

    this.baseLayerPicker = LayersConfig.baseLayers.length > 1 ? true : false

    if (LayersConfig.baseLayers.length === 1) {
      imageryProvider = new Cesium.WebMapServiceImageryProvider({
        url: LayersConfig.baseLayers[0].url,
        layers: LayersConfig.baseLayers[0].layers,
        enablePickFeatures: false
      });
    }

    viewerConf.viewerOptions = {
      selectionIndicator: true,
      timeline: false,
      infoBox: true,
      fullscreenButton: false,
      baseLayerPicker: this.baseLayerPicker,
      imageryProviderViewModels,
      imageryProvider,
      terrainProviderViewModels: [],
      animation: true,
      shouldAnimate: true,
      homeButton: true,
      geocoder: false,
      navigationHelpButton: true,
      navigationInstructionsInitiallyVisible: false,
    };

    viewerConf.viewerModifier = (view: any) => {
      view.screenSpaceEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK);
      view.bottomContainer.remove();
    };
  }

  handleMouseMove(viewer, e) {
    var ellipsoid = viewer.scene.globe.ellipsoid;
    // Mouse over the globe to see the cartographic position
    var rect = e.target.getBoundingClientRect();
    var relX = e.clientX - rect.left; //x position within the element.
    var relY = e.clientY - rect.top;  //y position within the element.
    var cartesian = viewer.camera.pickEllipsoid(new Cesium.Cartesian3(relX, relY), ellipsoid);
    if (cartesian) {
      var cartographic = ellipsoid.cartesianToCartographic(cartesian);
      var longitudeString = Cesium.Math.toDegrees(cartographic.longitude).toFixed(10);
      var latitudeString = Cesium.Math.toDegrees(cartographic.latitude).toFixed(10);
      var result = '(' + longitudeString + ', ' + latitudeString + ')';
      document.getElementById("coordsdisplay").innerHTML = result;
    }
  }

  ngAfterViewInit() {
    const viewer = this.acMap.getCesiumViewer();

    if (this.baseLayerPicker) {
      const baseLayerPickerViewModel = viewer.baseLayerPicker.viewModel;
      baseLayerPickerViewModel.selectedImagery = baseLayerPickerViewModel.imageryProviderViewModels[0];
    }

    viewer.scene.canvas.addEventListener('mousemove', e => this.handleMouseMove(viewer, e));

    const promise = Cesium.CzmlDataSource.load(this.doc);

    viewer.dataSources.add(promise);

    promise.then((result: any) => {
      const source = result;

      //Ingest packet data event
      this.dataService.getPacketDataObservable().subscribe((event: PacketEvent) => {
        if (this.sessionMap[event.session] == null) {
          this.sessionMap[event.session] = new Set();
        }

        if (!!event.packets) {
          source.process(event.packets);
          event.packets.forEach(packet => this.sessionMap[event.session].add(packet.id));
        } else {
          this.sessionMap[event.session].forEach(packetId => source.entities.removeById(packetId));
          this.sessionMap[event.session].clear();
        }
      });

      //Handle visibility change events
      this.dataService.getSetEntitiesVisibleObservable().subscribe(selectionEvent => {
        for (const id of selectionEvent.ids) {
          const entity = source.entities.getById(id);
          if (entity) {
            entity.show = selectionEvent.show;
          }
        }
      });

      //Handle entity selection events (zoom)
      this.dataService.getSelectionObservable().subscribe(event => {

        if (event.time) {
          //Shift a couple seconds to make up for precision and be sure we are into the availibility of the thing
          const time = new Date(event.time);
          time.setMilliseconds(time.getMilliseconds() + 2)
          viewer.clock.currentTime = Cesium.JulianDate.fromDate(time);
        }

        viewer.flyTo(source.entities.getById(event.id));
      });

      //Change the timeline time when the map time ticks
      viewer.clock.onTick.addEventListener((clock: any) => {
        this.timeService.sendMapTimeChanged(Cesium.JulianDate.toDate(clock.currentTime), false);
      });

      //Handle events telling the map to change time
      this.timeService.getTimelineTimeChangedObservable().subscribe(time => {
        viewer.clock.currentTime = Cesium.JulianDate.fromDate(time);
      });
    });
  }
}
