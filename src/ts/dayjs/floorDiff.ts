import {OpUnitType, PluginFunc} from "dayjs";

declare module 'dayjs' {
    interface Dayjs {
        /**
         * First floors both objects to the given unit using startOf, then calculates the diff
         * for that unit.
         */
        floorDiff(other: Dayjs, unit: OpUnitType): number
    }
}

export const floorDiff: PluginFunc = function (option, dayjsClass) {
    dayjsClass.prototype.floorDiff = function (other, unit) {
        return this.startOf(unit).diff(other.startOf(unit), unit);
    };
};
