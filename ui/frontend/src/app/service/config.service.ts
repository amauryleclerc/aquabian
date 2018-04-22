import { Injectable } from "@angular/core";


@Injectable()
export class ConfigService {

    constructor() {

    }

    public getBaseUrl(): string {
        return window.location.protocol + '//' + window.location.host;
    }

    public getWsBaseUrl(): string {

        let protocol = 'ws:';
        if (window.location.protocol === 'https:') {
            protocol = 'wss:';
        }
        return protocol + '//' + window.location.host;
    }



}