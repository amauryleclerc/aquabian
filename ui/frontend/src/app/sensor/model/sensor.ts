import { Measure } from './measure';

export class Sensor {

    public currentMeasure: Measure = null;

    constructor(public id: string,
        public name: string, public measures: Array<Measure>) {
    }

    public addMeasure(measure: Measure): void {
        this.measures.push(measure);
        if (this.currentMeasure == null || this.currentMeasure.date < measure.date) {
            this.currentMeasure = measure;
        }
    }
    public removeMeasure(measure: Measure): void {
        const index: number = this.measures.findIndex(m => measure === measure);
        if(index != -1){
            this.measures.splice(index);
        }
    }
}
