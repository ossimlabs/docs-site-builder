import { CommonModule } from '@angular/common';
import { NgModule, Optional, SkipSelf } from '@angular/core';

import { DataService } from './services/data.service';
import { TimeChangeService } from './services/time-change.service';

import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    BrowserModule,
    HttpClientModule
  ],
  providers: [
    TimeChangeService,
    DataService
  ]
})
export class CoreModule {

  constructor(@Optional() @SkipSelf() core: CoreModule) {
    if (core) {
      throw new Error('Cannot run!');
    }
  }
}
