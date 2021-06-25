export class TimeCardEntry {
    readonly id: string;
    readonly owner: string;
    readonly name: string;
    readonly lastCompleteTime: Date;
    readonly interval: number;

    constructor({id, owner, name, lastCompleteTime, interval}: Pick<TimeCardEntry, keyof TimeCardEntry>) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.lastCompleteTime = lastCompleteTime;
        this.interval = interval;
    }

    get expectedCompletionTime() {
        return new Date(this.lastCompleteTime.getTime() + this.interval);
    }

    get percentComplete() {
        const maxRange = this.interval;
        const nowRange = Date.now() - this.lastCompleteTime.getTime();
        return Math.min(100, 100 * (nowRange / maxRange));
    }
}
