import dayjs, {Dayjs} from "dayjs";
import {RRule} from "rrule";

export class TimeCardEntry {
    readonly id: string;
    readonly owner: string;
    readonly name: string;
    readonly lastCompleteTime: Dayjs;
    readonly interval: RRule;

    constructor({id, owner, name, lastCompleteTime, interval}: Pick<TimeCardEntry, keyof TimeCardEntry>) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.lastCompleteTime = lastCompleteTime;
        this.interval = interval;
    }

    get expectedCompletionTime(): Dayjs {
        return dayjs(this.interval.after(this.lastCompleteTime.toDate()));
    }

    get percentComplete() {
        const now = Date.now();
        const maxRange = now - this.expectedCompletionTime.valueOf();
        const nowRange = now - this.lastCompleteTime.valueOf();
        return Math.min(100, 100 * (nowRange / maxRange));
    }
}
