import {WeekdayStr} from "rrule";
import {oKeys} from "../utils";

export interface Weekday {
    short: string
    long: string
    key: WeekdayStr
}

function weekday(weekdayStr: WeekdayStr, long: string): Weekday {
    return {short: weekdayStr.charAt(0), long, key: weekdayStr};
}

export const ORDERED_DAYS: Weekday[] = [
    weekday("MO", "Monday"),
    weekday("TU", "Tuesday"),
    weekday("WE", "Wednesday"),
    weekday("TH", "Thursday"),
    weekday("FR", "Friday"),
    weekday("SA", "Saturday"),
    weekday("SU", "Sunday"),
];

export interface Month {
    name: string
    maximumDays: number
    /** 1-based index */
    index: number
}

function month(name: string, maximumDays: number, index: number): Month {
    return {name, maximumDays, index};
}

let index = 1;
export const Months = {
    JANUARY: month("January", 31, index++),
    FEBRUARY: month("February", 29, index++),
    MARCH: month("March", 31, index++),
    APRIL: month("April", 30, index++),
    MAY: month("May", 31, index++),
    JUNE: month("June", 30, index++),
    JULY: month("July", 31, index++),
    AUGUST: month("August", 31, index++),
    SEPTEMBER: month("September", 30, index++),
    OCTOBER: month("October", 31, index++),
    NOVEMBER: month("November", 30, index++),
    DECEMBER: month("December", 31, index++),
};

export const ORDERED_MONTHS: Month[] = oKeys(Months)
    .map(m => Months[m])
    .sort((a, b) => a.index - b.index);
