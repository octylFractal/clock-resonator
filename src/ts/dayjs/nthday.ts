import {PluginFunc} from "dayjs";


declare module 'dayjs' {
    interface Dayjs {
        /**
         * Retrieve N, where N satisfies the fact that {@link Dayjs#day} is the Nth weekday
         * of its kind in the month, e.g. for the 3rd Saturday, this returns 3.
         */
        nthday(): number
    }
}

export const nthday: PluginFunc = function (option, dayjsClass) {
    dayjsClass.prototype.nthday = function () {
        let count = 0;
        let currentDay = this;
        while (currentDay.month() == this.month()) {
            currentDay = currentDay.subtract(7, 'day');
            count++;
        }
        return count;
    };
};
