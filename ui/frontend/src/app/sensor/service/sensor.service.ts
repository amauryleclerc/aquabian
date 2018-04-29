import { Injectable, NgZone } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import 'rxjs/add/operator/retryWhen';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/repeatWhen';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/concat';
import 'rxjs/add/observable/from';

import { RxWebsocketClient } from '../../service/rx-websocket-client';
import { Sensor } from '../model/sensor';
import { Measure } from '../model/measure';
import { ConfigService } from '../../service/config.service';
import { AquabianConstants } from '../../aquabian.constants';
import { Chart } from 'angular-highcharts';
declare function require(path: string): any;
const sensorProjectionsEvents = require('../../../assets/js/fr/aquabian/projection/SensorProjectionEvents_pb.js');

@Injectable()
export class SensorService {

    private sensors: Map<String, Sensor> = new Map<String, Sensor>();
    private rangeSubject: Subject<Number> = new BehaviorSubject<Number>(60);
    private chart: Chart = new Chart({
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
        series: []
    });
    constructor(private websocketClient: RxWebsocketClient, private zone: NgZone, private configService: ConfigService) {

        this.rangeSubject.debounceTime(500)//
            .distinctUntilChanged()//
            .switchMap(second => websocketClient.create(configService.getWsBaseUrl() + AquabianConstants.SENSOR_PROJECTION_EVENT_STREAM_PATH + "?seconds=" + second))//
            .repeatWhen(obs => obs.delay(5000).do(v => console.log('repeat')))
            .map(e => sensorProjectionsEvents.SensorProjectionEvent.deserializeBinary(e))//
            .subscribe(e => this.handleSensorProjectionEvent(e)
                , e => console.error(e));

    }

    public getChart(): Chart {
        return this.chart;
    }

    public getRangeSeconds(): Observable<Number> {
        return this.rangeSubject;
    }
    public setRangeSeconds(second: Number): void {
        this.rangeSubject.next(second);
    }

    public getSensors(): Map<String, Sensor> {
        return this.sensors;
    }
  

    private handleSensorProjectionEvent(event: any): void {
        if (event.hasCurrentstateevent()) {
            this.handleCurrentstateevent(event.getCurrentstateevent());
        } else if (event.hasAddsensorevent()) {
            this.handleAddsensorevent(event.getAddsensorevent());
        } else if (event.hasAddmeasureevent()) {
            this.handleAddmeasureevent(event.getAddmeasureevent());
        }
    }

    private handleCurrentstateevent(event: any): void {
        console.log('handleCurrentstateevent');
        this.chart.ref.series.forEach(s =>{
            const serie: any = s;
            console.log(serie);
            this.chart.removeSerie(serie.index);
        } );
        event.getSensorsList()//
            .map(sensor => this.createSensor(sensor))//
            .forEach(sensor => {
                this.sensors.set(sensor.id, sensor);
                this.chart.addSerie({
                    name: sensor.name,
                    id: sensor.id,
                    data: []
                });
                sensor.measures.forEach(m =>  this.chart.addPoint([m.date.getTime(), m.value]), this.getSeriesIndex(sensor.id));
            });
        console.log(this.chart.ref.series.length);
    //    this.sensorsSubject.next(this.sensors);
    }
    private handleAddsensorevent(event: any): void {
        console.log('handleAddsensorevent');
        const sensor: Sensor = this.createSensor(event.getSensor());
        this.sensors.set(sensor.id, sensor);
     //   this.sensorsSubject.next(this.sensors);

    }

    private handleAddmeasureevent(event: any): void {
        console.log('handleAddmeasureevent');
        const sensor = this.sensors.get(event.getId());
        if(sensor != null){
            const measure: Measure = new Measure(event.getMeasure().getDate().toDate(), event.getMeasure().getValue());
            sensor.addMeasure(measure);
            const index: number = this.getSeriesIndex(event.getId());
            this.chart.addPoint([measure.date.getTime(), measure.value], index);
        }else{
            console.error('Sensor '+event.getId()+' not found');
        }

    }

    private createSensor(sensor: any): Sensor {
        const id: string = sensor.getId();
        const name: string = sensor.getName();
        const measures: Array<Measure> = sensor.getMeasuresList()//
            .map(measure => new Measure(measure.getDate().toDate(), measure.getValue()));
       // measures.forEach(m =>  this.chart.addPoint([m.date.getTime(), m.value]));
        console.log(sensor.getMeasuresList());
        console.log(measures); console.log(measures);
        return new Sensor(id, name, measures);
    }

     private getSeriesIndex(id: string): number{
        return (<any> this.chart.ref.get(id)).index;
     }

}
