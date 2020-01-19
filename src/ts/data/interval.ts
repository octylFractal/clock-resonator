import dayjs, {Dayjs} from "dayjs";
import {Frequency, Options, RRule, Weekday} from "rrule";
import {floorDiff} from "../dayjs/floorDiff";
import {nthday} from "../dayjs/nthday";

dayjs.extend(nthday);
dayjs.extend(floorDiff);

export function computeInterval(startDate: Dayjs, nextDate: Dayjs): RRule[] {
    const allOptions = new Array<Partial<Options>>();
    let byMonthOptions: Partial<Options> | undefined;
    if (startDate.month() == nextDate.month()) {
        const yearDiff = nextDate.floorDiff(startDate, "year");
        byMonthOptions = {
            freq: Frequency.YEARLY,
            interval: yearDiff,
            bymonth: startDate.month() + 1,
        };
    }
    const monthDiff = nextDate.floorDiff(startDate, "month");
    if (monthDiff % 12 != 0) {
        // Non-year month intervals
        byMonthOptions = {
           freq: Frequency.MONTHLY,
           interval: monthDiff,
        };
    }
    if (byMonthOptions !== undefined) {
        if (startDate.date() == nextDate.date()) {
            // every <month interval> on Month Date
            allOptions.push({
                ...byMonthOptions,
                bymonthday: startDate.date()
            });
        }
        if (startDate.day() == nextDate.day()) {
            if (startDate.nthday() == nextDate.nthday()) {
                // every <month interval> on Day of Week of Month
                allOptions.push({
                    ...byMonthOptions,
                    byweekday: dayjsToWeekday(startDate)
                });
            }
        }
    }
    return allOptions.map(opt => new RRule(opt));
}

function dayjsToWeekday(date: Dayjs): Weekday {
    let day = date.day() - 1;
    if (day < 0) {
        day += 7;
    }
    return new Weekday(day, date.nthday());
}
