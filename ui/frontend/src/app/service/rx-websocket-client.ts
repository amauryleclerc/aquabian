import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import 'rxjs/add/operator/retryWhen';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';

@Injectable()
export class RxWebsocketClient {
    constructor() { }

    public create(url): Observable<any> {
        return Observable.create(
            (obs: Observer<MessageEvent>) => {
                const ws = new WebSocket(url);
                ws.onmessage = obs.next.bind(obs);
                ws.onerror = obs.error.bind(obs);
                ws.onclose = obs.complete.bind(obs);
                return ws.close.bind(ws);
            })//
            .mergeMap(e => Observable.create((obs: Observer<MessageEvent>) => {
                const fileReader = new FileReader();
                fileReader.onload = (event) => {
                    const target: any = event.target;
                    obs.next(target.result);
                    obs.complete();
                };
                fileReader.onerror = (event) => {
                    obs.error(event);
                };
                fileReader.readAsArrayBuffer(e.data);
            }))//
            .do(event => null, error => console.error(error))//
            .retryWhen(obs => obs.delay(5000));
    }

}