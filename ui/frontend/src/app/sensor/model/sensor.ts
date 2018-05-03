import { Measure } from './measure';

export class Sensor {

    public currentMeasure: Measure = null;
    public maxMeasure: Measure = null;
    public minMeasure: Measure = null;

    constructor(public id: string,
        public name: string, public measures: Array<Measure>) {
        this.currentMeasure = measures.reduce((p, c) => {
            if (p == null || p.date.getTime() < c.date.getTime()) {
                return c;
            }
            return p;
        });
        this.maxMeasure = measures.reduce((p, c) => {
            if (p == null || p.value < c.value) {
                return c;
            }
            return p;
        });
        this.minMeasure = measures.reduce((p, c) => {
            if (p == null || p.value > c.value) {
                return c;
            }
            return p;
        });
    }

    public addMeasure(measure: Measure): void {
        this.measures.push(measure);
        if (this.currentMeasure == null || this.currentMeasure.date < measure.date) {
            this.currentMeasure = measure;
        }
        if(this.maxMeasure == null || this.maxMeasure.value < measure.value){
            this.maxMeasure = measure;
        }
        if(this.minMeasure == null || this.minMeasure.value > measure.value){
            this.minMeasure = measure;
        }
    }
    public removeMeasure(measure: Measure): void {
        const index: number = this.measures.findIndex(m => measure === measure);
        if (index != -1) {
            this.measures.splice(index);
        }
        if(this.minMeasure != null && this.minMeasure.date == measure.date){
            this.minMeasure = null;
        }
        if(this.maxMeasure != null && this.maxMeasure.date == measure.date){
            this.maxMeasure = null;
        }
    }
}
