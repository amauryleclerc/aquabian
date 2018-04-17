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
import { RxWebsocketClient } from '../../service/rx-websocket-client';
import { Sensor } from '../model/sensor';
declare function require(path: string): any;
const sensorProjectionsEvents = require('../../../assets/js/fr/aquabian/projection/SensorProjectionEvents_pb.js');

@Injectable()
export class SprintsService {

    private sensors: Map<String, Sensor> = new Map<String, Sensor>();
    private sensorsSubject: Subject<Map<String, Sensor>> = new BehaviorSubject<Map<String, Sensor>>(this.sensors);

    constructor(private websocketClient: RxWebsocketClient, private zone: NgZone) {

        websocketClient.create('ws://localhost:4200/aquabian/projection/sensor/events')//
            .map(e => sensorProjectionsEvents.SensorProjectionEvent.deserializeBinary(e))//
            .subscribe(e => this.handleSensorProjectionEvent(e)
                , e => console.error(e));

    }

    public getSprints(): Map<String, Sensor> {
        return this.sensors;
    }
    public getSprintsStream(): Observable<Map<String, Sensor>> {
        return this.sensorsSubject;
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
    }
    private handleAddsensorevent(event: any): void {
        console.log('handleAddsensorevent');
   }

   private handleAddmeasureevent(event: any): void {
    console.log('handleAddsensorevent');
    }

}
