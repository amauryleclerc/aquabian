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

const appRoutes: Routes = [
   { path: 'sensors', component: SensorsComponent },
   { path: '', redirectTo: '/sensors', pathMatch: 'full' },
   { path: '*', redirectTo: '/sensors', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    AppComponent,
    SensorsComponent
  ],
  imports: [
    BrowserModule,
    BrowserModule,
    HttpClientModule,
    NgbModule.forRoot(),
    RouterModule.forRoot(appRoutes, { useHash: true }),
    ChartModule
  ],
  providers: [RxWebsocketClient, SensorService],
  bootstrap: [AppComponent]
})
export class AppModule { }
