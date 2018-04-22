import { Component, NgZone, ChangeDetectionStrategy } from '@angular/core';
import { Measure } from '../model/measure';
import { SensorService } from '../service/sensor.service';
import { Sensor } from '../model/sensor';
import { Chart } from 'angular-highcharts';

@Component({
  templateUrl: './sensors.component.html'
})
export class SensorsComponent {
  sensors: Array<Sensor> = new Array<Sensor>();
  chart = new Chart({
    chart: {
      type: 'line'
    },
    title: {
      text: 'Linechart'
    },
    credits: {
      enabled: false
    },
    xAxis: {
        type: 'datetime',
        dateTimeLabelFormats: { // don't display the dummy year
            month: '%e. %b',
            year: '%b'
        },
        title: {
            text: 'Date'
        }
    },
    series: [{
      name: 'Line 1',
      data: []
    }]
  });

  constructor(private sensorService: SensorService) {
    sensorService.getSensorsStream().subscribe(m => {
setTimeout(() => {
        this.sensors = Array.from(m.values());
        console.log(this.sensors);
});

    } , e => console.error(e));

    sensorService.getMeasureStream().subscribe(measures => {
        setTimeout(() => {
          measures.forEach(m =>this.chart.addPoint([m.date.getTime(), m.value]));
        });
    });
  }


}
