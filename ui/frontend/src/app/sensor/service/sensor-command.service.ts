import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import 'rxjs/add/operator/retryWhen';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/mergeMap';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ConfigService } from '../../service/config.service';
import { AquabianConstants } from '../../aquabian.constants';
declare function require(path: string): any;
const aquabianCommands = require('../../../assets/js/fr/aquabian/domain/command/AquabianCommands_pb.js');


@Injectable()
export class SensorCommandService {


    constructor(private httpClient: HttpClient,  private configService: ConfigService) {
    }
    
    public renameSensor(id: string, name: string): Observable<any> {
        const command =  new aquabianCommands.AquabianCommand();
        const renameSensorCommand =  new aquabianCommands.RenameSensorCommand();
        renameSensorCommand.setId(id);
        renameSensorCommand.setName(name);
        command.setRenamesensorcommand(renameSensorCommand);
        const bin =  command.serializeBinary();
        return this.httpClient.post<any>(this.configService.getBaseUrl()+ AquabianConstants.COMMAND_PATH,bin.buffer);
    }





}