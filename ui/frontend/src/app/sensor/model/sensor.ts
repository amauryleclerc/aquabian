import { Measure } from './measure';

export class Sensor {

    constructor(public id: string,
        public name: string, public measures: Array<Measure>) {
    }

}
