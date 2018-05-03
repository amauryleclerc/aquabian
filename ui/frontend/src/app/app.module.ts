import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ChartModule } from 'angular-highcharts';
import {RxWebsocketClient} from './service/rx-websocket-client';
import {SensorService} from './sensor/service/sensor.service';
import { SensorsComponent } from './sensor/component/sensors.component';
import { ConfigService } from './service/config.service';
import { FormsModule } from '@angular/forms';
import * as Highcharts from 'highcharts/highstock';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

registerLocaleData(localeFr);
const appRoutes: Routes = [
   { path: 'sensors', component: SensorsComponent },
   { path: '', redirectTo: '/sensors', pathMatch: 'full' },
   { path: '*', redirectTo: '/sensors', pathMatch: 'full' }
];
Highcharts.setOptions({
  global: {
    timezoneOffset: new Date().getTimezoneOffset()
  }
});

@NgModule({
  declarations: [
    AppComponent,
    SensorsComponent
  ],
  imports: [
    FormsModule,
    BrowserModule,
    BrowserModule,
    HttpClientModule,
    NgbModule.forRoot(),
    RouterModule.forRoot(appRoutes, { useHash: true }),
    ChartModule
  ],
  providers: [RxWebsocketClient, SensorService, ConfigService ],
  bootstrap: [AppComponent]
})
export class AppModule { }
