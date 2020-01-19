import dayjs, {Dayjs} from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";

dayjs.extend(relativeTime);

export function prettyDate(date: Dayjs): string {
    const now = dayjs();
    if (now.isSame(date)) {
        const secondsDiff = now.diff(date, "second");
        if (0 <= secondsDiff && secondsDiff <= 5) {
            return "right now";
        } else if (-5 <= secondsDiff && secondsDiff < 0) {
            return "very soon";
        }
        // close enough to use relative dating
        const dist = now.from(date, true);
        if (date.isAfter(now)) {
            return "in " + dist;
        }
        return dist + " ago";
    }
    // e.g. 25 June 2018, 1:00 PM
    return "at " + date.format("dd mmmm yyyy, h:MM TT");
}