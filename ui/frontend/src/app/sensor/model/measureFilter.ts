
export class MeasureFilter {

    private constructor(private isPast: boolean, private isSlidingWindow: boolean, private dateMin: Date,
        private dateMax: Date, private afterglow: number) {

    }


    public static createForPastWindow(dateMin: Date, dateMax: Date): MeasureFilter {
        return new MeasureFilter(true, false, dateMin, dateMax, 0);
    }

    public static createForSlidingWindow(afterglow: number): MeasureFilter {
        return new MeasureFilter(false, true, null, null, afterglow);
    }


    public getQuery(): string {
        if (this.isSlidingWindow) {
            return "?afterglow=" + this.afterglow;
        } else {
            return "?dateMin=" + this.dateMin.getTime()+"&dateMax="+this.dateMax.getTime();
        }
    }

    public getIsPast(): boolean{
        return this.isPast;
    }

    public getAfterglow(): number {
        return this.afterglow;
    }

    public getDateMax(): Date{
        return this.dateMax;
    }
    public getDateMin(): Date{
        return this.dateMin;
    }
}
