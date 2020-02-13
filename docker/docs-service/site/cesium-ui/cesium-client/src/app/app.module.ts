import { NgModule, DoBootstrap } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { InjectableRxStompConfig, RxStompService, rxStompServiceFactory } from '@stomp/ng2-stompjs';
import { AngularCesiumModule } from 'angular-cesium';
import { HttpClientModule } from '@angular/common/http';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatCardModule } from '@angular/material/card';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BannerComponent } from './classification-banner/banner.component';
import { CesiumMapComponent } from './cesium-map/cesium-map.component';
import { CoreModule } from './core/core.module';
import { RxStompConfig } from './rx-stomp.config';
import { TimelineComponent } from './timeline/timeline.component';
import { LayerTreeComponent } from './layer-tree/layer-tree.component';
import { MatTreeModule } from '@angular/material/tree';
import { MatIconModule, MatInputModule, MatButtonModule, MatCheckboxModule, MatDatepickerModule, MatNativeDateModule } from '@angular/material/';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations'
import { KeycloakService, KeycloakAngularModule } from 'keycloak-angular';
import { environment } from '../environments/environment';
import { AccessDeniedComponent } from './access-denied/access-denied.component';


//Services
import { DataService } from './core/services/data.service';
import { UserService } from './core/services/user.service';


const keycloakService: KeycloakService = new KeycloakService();

@NgModule({
  declarations: [
    AppComponent,
    BannerComponent,
    CesiumMapComponent,
    TimelineComponent,
    LayerTreeComponent,
    AccessDeniedComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    AngularCesiumModule.forRoot(),
    CoreModule,
    MatTreeModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    DragDropModule,
    MatNativeDateModule,
    MatDatepickerModule,
    BrowserAnimationsModule,
    KeycloakAngularModule,
    MatCardModule
  ],
  exports: [
  ],
  providers: [
    DataService,
    UserService,
    {
      provide: InjectableRxStompConfig,
      useValue: RxStompConfig
    },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    },
    {
      provide: KeycloakService,
      useValue: keycloakService
    }
  ],
  entryComponents: [
    AppComponent
  ]

})
export class AppModule  implements DoBootstrap {
  async ngDoBootstrap(app) {
    const { KEYCLOAK_CONFIG } = environment;

    try {
      if (environment.KEYCLOACK_ACTIVATE)     
        await keycloakService.init({ config: KEYCLOAK_CONFIG });     
      app.bootstrap(AppComponent);
    } catch (error) {
      console.error('Keycloak init failed', error);
    }
  }
}
