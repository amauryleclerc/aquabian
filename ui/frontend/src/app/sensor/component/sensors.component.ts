import { Component, NgZone, ChangeDetectionStrategy } from '@angular/core';
import { Measure } from '../model/measure';
import { SensorService } from '../service/sensor.service';
import { Sensor } from '../model/sensor';
import { Chart } from 'angular-highcharts';
import { MeasureFilter } from '../model/measureFilter';

@Component({
  templateUrl: './sensors.component.html'
})
export class SensorsComponent {
  chart: Chart = null;
  sensors: Array<Sensor> = null;
  measureFilter: MeasureFilter = null;
  filterType: string = "pastWindow";
  afterglow: number = 60;
  dateMin: string = this.convertDateToString(new Date());
  dateMax: string = this.convertDateToString(new Date());
  constructor(private sensorService: SensorService) {
    sensorService.getMeasureFilter().subscribe(measureFilter => this.onFilterChange(measureFilter), e => console.error(e));
    this.chart = sensorService.getChart();
    this.sensors = sensorService.getSensors();
   
  }

  private onFilterChange(measureFilter: MeasureFilter) {
    this.measureFilter = measureFilter;
    if (measureFilter.getIsPast()) {
      this.filterType = "pastWindow";
      this.dateMin = this.convertDateToString(measureFilter.getDateMin());
      this.dateMax = this.convertDateToString(measureFilter.getDateMax());
    } else {
      this.filterType = "slidingWindow";
      this.afterglow = measureFilter.getAfterglow();
    }
  }

  public onRadioChange(event) {
    console.log("radio change to " + event.target.id);
    if (event.target.id === "pastWindow") {
     this.sensorService.setMeasureFilter(MeasureFilter.createForPastWindow(new Date(this.dateMin),new Date(this.dateMax)));
    } else {
      this.sensorService.setMeasureFilter(MeasureFilter.createForSlidingWindow(this.afterglow));
    }

  }

  public onAfterglowChange(event) {
    console.log("afterglow change to " + event);
    this.sensorService.setMeasureFilter(MeasureFilter.createForSlidingWindow(event));
  }
  public onDateMinChange(event) {
    console.log("Date min change to " + event.target.value);
    this.sensorService.setMeasureFilter(MeasureFilter.createForPastWindow(new Date(event.target.value),new Date(this.dateMax)));
  }

  public onDateMaxChange(event) {
    console.log("Date max change to " + event.target.value);
    this.sensorService.setMeasureFilter(MeasureFilter.createForPastWindow(new Date(this.dateMin),new Date(event.target.value)));
  }


  private convertDateToString(date: Date): string{
    return (date.getFullYear().toString() + '-' 
    + ("0" + (date.getMonth() + 1)).slice(-2) + '-' 
    + ("0" + (date.getDate())).slice(-2))
    + 'T' + date.toTimeString().slice(0,5);
  }


}
