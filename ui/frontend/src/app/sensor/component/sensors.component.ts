import { Component, NgZone, ChangeDetectionStrategy } from '@angular/core';
import { Measure } from '../model/measure';
import { SensorService } from '../service/sensor.service';
import { Sensor } from '../model/sensor';
import { Chart } from 'angular-highcharts';

@Component({
  templateUrl: './sensors.component.html'
})
export class SensorsComponent {
  chart: Chart= null;
  sensors: Array<Sensor> = null;
  range: Number = 0;
  constructor(private sensorService: SensorService) {
    sensorService.getRangeSeconds().subscribe(range => this.range = range, e => console.error(e));
    this.chart = sensorService.getChart();
    this.sensors = sensorService.getSensors();
  }

  public rangeChange(event: Number){
    this.sensorService.setRangeSeconds(event);
  }

}
