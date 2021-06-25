import produce from "immer";
import React from "react";
import {FormGroup, Label} from "reactstrap";
import {WeekdayStr} from "rrule";
import {ORDERED_DAYS} from "../../data/time";
import {TextCheckBox} from "../TextCheckBox";

export interface WeekIntervalInputProps {
    weekdays: Set<WeekdayStr>

    setWeekdays(weekdays: Set<WeekdayStr>): void
}

export const WeekIntervalInput: React.FC<WeekIntervalInputProps> = (
    {weekdays, setWeekdays}
) => {

    function setWeekday(weekdayStr: WeekdayStr, on: boolean) {
        const next = produce(weekdays, draft => {
            if (on) {
                draft.add(weekdayStr);
            } else {
                draft.delete(weekdayStr);
            }
        });
        setWeekdays(next);
    }

    return <FormGroup>
        <Label>Repeat on</Label>
        <div className="text-check-box-wrapper form-control p-0">
            {Array.from(ORDERED_DAYS).map(day => {
                const isEnabled = weekdays.has(day.key);
                return <TextCheckBox shortLabel={day.short}
                                     longLabel={day.long}
                                     checked={isEnabled}
                                     setChecked={value => setWeekday(day.key, value)}
                                     key={day.key}/>;
            })}
        </div>
    </FormGroup>;
};
