import produce from "immer";
import React, {useRef} from "react";
import {Col, Container, Input, Label, Row} from "reactstrap";
import {WeekdayStr} from "rrule";
import {ORDERED_DAYS} from "../../data/time";

export enum DayRecurrenceType {
    MONTH_DAY = "month-day",
    NTH_WEEK_DAY = "nth-week-day",
}

export interface MonthDayRecurrence {
    monthDay: number
}

export interface NthWeekDayRecurrence {
    weekday: WeekdayStr
    nth: number
}

export interface DayRecurrence {
    type: DayRecurrenceType
    monthDay: MonthDayRecurrence
    nthWeekDay: NthWeekDayRecurrence
}

export interface MonthIntervalInputProps {
    recurrenceDay: DayRecurrence

    setRecurrenceDay(recurrence: DayRecurrence): void
}

function monthDayOption(recurrenceDay: DayRecurrence,
                        setRecurrenceDay: (recurrence: DayRecurrence) => void) {
    return <span>
        On day #
        <Input className="inline" type="number" min={1} max={31}
               value={recurrenceDay.monthDay.monthDay}
               onChange={e => setRecurrenceDay(produce(recurrenceDay, draft => {
                   draft.monthDay.monthDay = e.target.valueAsNumber;
               }))}/>
    </span>;
}

function nthWeekDayOption(recurrenceDay: DayRecurrence,
                          setRecurrenceDay: (recurrence: DayRecurrence) => void) {
    return <span>
        On
        <Input className="inline" type="select"
               value={recurrenceDay.nthWeekDay.weekday}
               onChange={e => setRecurrenceDay(produce(recurrenceDay, draft => {
                   draft.nthWeekDay.weekday = e.target.value as WeekdayStr;
               }))}>
            {Array.from(ORDERED_DAYS).map(day => {
                return <option value={day.key} key={day.key}>
                    {day.long}
                </option>;
            })}
        </Input>
        #
        <Input className="inline" type="number" min={1} max={5}
               value={recurrenceDay.nthWeekDay.nth}
               onChange={e => setRecurrenceDay(produce(recurrenceDay, draft => {
                   draft.nthWeekDay.nth = e.target.valueAsNumber;
               }))}/>
    </span>;
}

export const MonthIntervalInput: React.FC<MonthIntervalInputProps> = (
    {recurrenceDay, setRecurrenceDay}
) => {
    const {current: idBase} = useRef(Math.random());

    function setDayType(type: DayRecurrenceType) {
        setRecurrenceDay(produce(recurrenceDay, draft => {
            draft.type = type;
        }));
    }

    const isMonthDay = recurrenceDay.type === DayRecurrenceType.MONTH_DAY;
    return <Container className="py-3">
        <Row form>
            <Col>
                <Label>
                    <input type="radio"
                           className="mr-3"
                           checked={isMonthDay}
                           onChange={e => e.target.checked && setDayType(DayRecurrenceType.MONTH_DAY)}
                           name="recurrence-day-type"/>
                    {monthDayOption(recurrenceDay, setRecurrenceDay)}
                </Label>
            </Col>
        </Row>
        <Row form>
            <Col>
                <Label>
                    <input type="radio"
                           className="mr-3"
                           checked={!isMonthDay}
                           onChange={e => e.target.checked && setDayType(DayRecurrenceType.NTH_WEEK_DAY)}
                           name="recurrence-day-type"/>
                    {nthWeekDayOption(recurrenceDay, setRecurrenceDay)}
                </Label>
            </Col>
        </Row>
    </Container>;
};