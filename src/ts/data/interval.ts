import dayjs, {Dayjs} from "dayjs";
import {Frequency, Options, RRule, Weekday} from "rrule";
import {floorDiff} from "../dayjs/floorDiff";
import {nthday} from "../dayjs/nthday";

dayjs.extend(nthday);
dayjs.extend(floorDiff);

export function computeInterval(startDate: Dayjs, nextDate: Dayjs): RRule[] {
    const allOptions = new Array<Partial<Options>>();
    addMonthlyOptions(startDate, nextDate, allOptions);
    addWeeklyOptions(startDate, nextDate, allOptions);
    addDailyOptions(startDate, nextDate, allOptions);
    return allOptions.map(opt => new RRule(opt));
}

function addMonthlyOptions(startDate: dayjs.Dayjs, nextDate: dayjs.Dayjs, allOptions: Partial<Options>[]) {
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
                    byweekday: dayjsToWeekday(startDate, {
                        includeNth: true
                    })
                });
            }
        }
    }
}

function addWeeklyOptions(startDate: dayjs.Dayjs, nextDate: dayjs.Dayjs, allOptions: Partial<Options>[]) {
    const weekDiff = nextDate.floorDiff(startDate, "week");
    const byWeekOptions: Partial<Options> = {
        freq: Frequency.WEEKLY,
        interval: weekDiff,
    };
    if (startDate.day() == nextDate.day() && startDate.nthday() != nextDate.nthday()) {
        // every <week interval> on day of week
        // (only add if nthday is not aligned)
        allOptions.push({
            ...byWeekOptions,
            byweekday: dayjsToWeekday(startDate)
        })
    }
}

function addDailyOptions(startDate: dayjs.Dayjs, nextDate: dayjs.Dayjs, allOptions: Partial<Options>[]) {
    allOptions.push({
        freq: Frequency.DAILY,
        interval: nextDate.floorDiff(startDate, 'day')
    });
}

interface DayjsToWeekdayOptions {
    includeNth?: boolean
}

function dayjsToWeekday(date: Dayjs, options? : DayjsToWeekdayOptions): Weekday {
    let day = date.day() - 1;
    if (day < 0) {
        day += 7;
    }
    return new Weekday(day, options?.includeNth ? date.nthday() : undefined);
}
