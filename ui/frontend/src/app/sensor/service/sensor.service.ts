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
import 'rxjs/add/operator/repeat';
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
import { DataPoint, SeriesObject } from 'highcharts';
import { MeasureFilter } from '../model/measureFilter';
declare function require(path: string): any;
const sensorProjectionsEvents = require('../../../assets/js/fr/aquabian/projection/SensorProjectionEvents_pb.js');

@Injectable()
export class SensorService {

    private sensors: Map<String, Sensor> = new Map<String, Sensor>();
    private sensorsList: Array<Sensor> = new Array<Sensor>();
    private measureFilterSubject: Subject<MeasureFilter> = new BehaviorSubject<MeasureFilter>(MeasureFilter.createForSlidingWindow(60));
    private chart: Chart = new Chart({
        chart: {
            type: 'line'
        },
        title: {
            text: 'Temperature'
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

        this.measureFilterSubject.debounceTime(500)//
            .distinctUntilChanged()//
            .switchMap(filter => websocketClient.create(configService.getWsBaseUrl() + AquabianConstants.SENSOR_PROJECTION_EVENT_STREAM_PATH + filter.getQuery()))//
            .map(e => sensorProjectionsEvents.SensorProjectionEvent.deserializeBinary(e))//
            .subscribe(e => this.handleSensorProjectionEvent(e)
                , e => console.error(e), () => console.error("complete"));

    }

    public getChart(): Chart {
        return this.chart;
    }
    public getSensors(): Array<Sensor> {
        return this.sensorsList;
    }


    public getMeasureFilter(): Observable<MeasureFilter> {
        return this.measureFilterSubject;
    }
    public setMeasureFilter(filter: MeasureFilter): void {
        this.measureFilterSubject.next(filter);
    }

    private handleSensorProjectionEvent(event: any): void {
        if (event.hasCurrentstateevent()) {
            this.handleCurrentstateevent(event.getCurrentstateevent());
        } else if (event.hasAddsensorevent()) {
            this.handleAddsensorevent(event.getAddsensorevent());
        } else if (event.hasAddmeasureevent()) {
            this.handleAddmeasureevent(event.getAddmeasureevent());
        } else if (event.hasRemovemeasureevent()) {
            this.handleRemovemeasureevent(event.getRemovemeasureevent());
        } else if (event.hasRemovemeasureevent()) {
            this.handleRemovemeasureevent(event.getRemovemeasureevent());
        } else if (event.hasSensorrenamedevent()) {
            this.handleSensorrenamedevent(event.getSensorrenamedevent());
        }
    }

    private handleCurrentstateevent(event: any): void {
        console.log('handleCurrentstateevent');
        const series = [...this.chart.ref.series];
        series.reverse().forEach(s => {
            const serie: any = s;
            this.chart.removeSerie(serie.index);
        });
        this.sensorsList.splice(0, this.sensorsList.length - 1);
        this.sensors.clear();
        event.getSensorsList()//
            .map(sensor => this.createSensor(sensor))//
            .forEach(sensor => {
                this.sensors.set(sensor.id, sensor);
                this.sensorsList.push(sensor);
                this.chart.addSerie({
                    name: sensor.name,
                    id: sensor.id,
                    data: sensor.measures.map(m => <DataPoint>{
                        x: m.date.getTime(),
                        y: m.value,
                        id: sensor.id + "_" + m.date.toString()
                    })
                });
            });
    }
    private handleAddsensorevent(event: any): void {
        console.log('handleAddsensorevent');
        const sensor: Sensor = this.createSensor(event.getSensor());
        this.sensors.set(sensor.id, sensor);
        this.sensorsList.push(sensor);
        this.chart.addSerie({
            name: sensor.name,
            id: sensor.id,
            data: []
        });

    }
    private handleRemovemeasureevent(event: any): void {
        console.log('handleRemovemeasureevent');
        const sensor: Sensor = this.sensors.get(event.getId());
        if (sensor != null) {
            const measure: Measure = new Measure(event.getMeasure().getDate().toDate(), event.getMeasure().getValue());
            sensor.removeMeasure(measure);
            const index: number = this.getSeriesIndex(event.getId());
            const id: string = sensor.id + "_" + measure.date.toString();
            const point = <any>this.chart.ref.get(id);
            if (point != null) {
                this.chart.removePoint(point.index, index);
            } else {
                console.error('Point ' + id + ' not found');
                console.error(event.toObject());
            }

        } else {
            console.error('Sensor ' + event.getId() + ' not found');
        }

    }
    private handleAddmeasureevent(event: any): void {
        console.log('handleAddmeasureevent');
        //   this.chart.ref.series[0].setVisible(false);
        const sensor = this.sensors.get(event.getId());
        if (sensor != null) {
            const m: Measure = new Measure(event.getMeasure().getDate().toDate(), event.getMeasure().getValue());
            sensor.addMeasure(m);
            const index: number = this.getSeriesIndex(event.getId());
            const point: DataPoint = <DataPoint>{
                x: m.date.getTime(),
                y: m.value,
                id: sensor.id + "_" + m.date.toString()
            }
            this.chart.addPoint(point, index);
        } else {
            console.error('Sensor ' + event.getId() + ' not found');
        }

    }

    private handleSensorrenamedevent(event: any): void {
        const sensor = this.sensors.get(event.getId());
        if (sensor != null) {
            var serie: SeriesObject;
            sensor.name = event.getName();
            const s: any = this.chart.ref.get(event.getId());
            s.update({name:event.getName()});
        } else {
            console.error('Sensor ' + event.getId() + ' not found');
        }
    }

    private createSensor(sensor: any): Sensor {
        const id: string = sensor.getId();
        const name: string = sensor.getName();
        const measures: Array<Measure> = sensor.getMeasuresList()//
            .map(measure => new Measure(measure.getDate().toDate(), measure.getValue()));
        return new Sensor(id, name, measures);
    }

    private getSeriesIndex(id: string): number {
        return (<any>this.chart.ref.get(id)).index;
    }

}
