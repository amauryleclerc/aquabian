import { Injectable, NgZone } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable} from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import 'rxjs/add/operator/retryWhen';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/repeatWhen';
import 'rxjs/add/operator/concat';
import 'rxjs/add/observable/from';
import { RxWebsocketClient } from '../../service/rx-websocket-client';
import { Sensor } from '../model/sensor';
import { Measure } from '../model/measure';
declare function require(path: string): any;
const sensorProjectionsEvents = require('../../../assets/js/fr/aquabian/projection/SensorProjectionEvents_pb.js');

@Injectable()
export class SensorService {

    private sensors: Map<String, Sensor> = new Map<String, Sensor>();
    private sensorsSubject: Subject<Map<String, Sensor>> = new BehaviorSubject<Map<String, Sensor>>(this.sensors);
    private measureSubject: Subject<Measure> = new Subject<Measure>();

    constructor(private websocketClient: RxWebsocketClient, private zone: NgZone) {

        websocketClient.create('ws://localhost:4200/aquabian/projection/sensor/events')//
            .repeatWhen(obs => obs.delay(5000).do(v => console.log('repeat')))
            .map(e => sensorProjectionsEvents.SensorProjectionEvent.deserializeBinary(e))//
            .subscribe(e => this.handleSensorProjectionEvent(e)
                , e => console.error(e));

    }

    public getSensors(): Map<String, Sensor> {
        return this.sensors;
    }
    public getSensorsStream(): Observable<Map<String, Sensor>> {
        return this.sensorsSubject;
    }
    public getMeasureStream(): Observable<Measure> {
       return  Observable.from(Array.from(this.sensors.values())) //
        .mergeMap(sensor =>   Observable.from(sensor.measures))//
        .concat(this.measureSubject);
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
         event.getSensorsList()//
         .map(sensor => this.createSensor(sensor))//
         .forEach(sensor => {
            this.sensors.set(sensor.id, sensor);
         });
         this.sensorsSubject.next(this.sensors);
    }
    private handleAddsensorevent(event: any): void {
        console.log('handleAddsensorevent');
        const sensor: Sensor = this.createSensor(event.getSensor());
        this.sensors.set(sensor.id, sensor);
        this.sensorsSubject.next(this.sensors);

   }

   private handleAddmeasureevent(event: any): void {
        console.log('handleAddmeasureevent');
      const measure: Measure =  new Measure(event.getMeasure().getDate().toDate(), event.getMeasure().getValue());
        this.sensors.get(event.getId()).measures.push(measure);
     //   this.sensorsSubject.next(this.sensors);
     this.measureSubject.next(measure);
    }

    private createSensor(sensor: any): Sensor {
        const id: string = sensor.getId();
        const name: string = sensor.getName();
        const measures: Array<Measure> = sensor.getMeasuresList()//
        .map(measure => new Measure(measure.getDate().toDate(), measure.getValue()));
        measures.forEach(m => this.measureSubject.next(m));
        console.log(sensor.getMeasuresList());
        console.log(measures);   console.log(measures);
        return new Sensor(id, name, measures);
    }

}