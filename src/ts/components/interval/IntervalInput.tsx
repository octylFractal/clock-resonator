import classNames from "classnames";
import {Dayjs} from "dayjs";
import React, {useState} from "react";
import {Button, ButtonProps, Col, Container, Input, Row} from "reactstrap";
import {Frequency, Options, RRule, Weekday, WeekdayStr} from "rrule";
import {Months} from "../../data/time";
import {exhaustiveCheck} from "../../utils";
import {SimpleModal} from "../SimpleModal";
import {DayRecurrence, DayRecurrenceType, MonthIntervalInput, MonthIntervalInputProps} from "./MonthIntervalInput";
import {WeekIntervalInput, WeekIntervalInputProps} from "./WeekIntervalInput";
import {YearIntervalInput, YearIntervalInputProps} from "./YearIntervalInput";

export enum GeneralPeriod {
    DAY = "day",
    WEEK = "week",
    MONTH = "month",
    YEAR = "year",
}

const GP_TO_RR_FREQUENCY: Record<GeneralPeriod, Frequency> = {
    [GeneralPeriod.DAY]: Frequency.DAILY,
    [GeneralPeriod.WEEK]: Frequency.WEEKLY,
    [GeneralPeriod.MONTH]: Frequency.MONTHLY,
    [GeneralPeriod.YEAR]: Frequency.YEARLY,
};

const displayGeneralPeriod: Record<GeneralPeriod, string> = {
    [GeneralPeriod.DAY]: GeneralPeriod.DAY,
    [GeneralPeriod.WEEK]: GeneralPeriod.WEEK,
    [GeneralPeriod.MONTH]: GeneralPeriod.MONTH,
    [GeneralPeriod.YEAR]: GeneralPeriod.YEAR,
};

interface SubPeriodProps {
    week: WeekIntervalInputProps
    month: MonthIntervalInputProps
    year: YearIntervalInputProps
}

function getGeneralPeriodInput(generalPeriod: GeneralPeriod,
                               {week, month, year}: SubPeriodProps): React.ReactElement | undefined {
    switch (generalPeriod) {
        case GeneralPeriod.DAY:
            return undefined;
        case GeneralPeriod.WEEK:
            return <WeekIntervalInput {...week} />;
        case GeneralPeriod.MONTH:
            return <MonthIntervalInput {...month} />;
        case GeneralPeriod.YEAR:
            return <YearIntervalInput {...year} />;
    }
}

interface IntervalInputModalProps {
    startDate: Dayjs | undefined

    generalPeriod: GeneralPeriod

    setGeneralPeriod(generalPeriod: GeneralPeriod): void

    /**
     * Number of {@field generalPeriod}s between occurrences.
     */
    periods: number

    setPeriods(periods: number): void

    subPeriod: SubPeriodProps
}

const IntervalInputModal: React.FC<IntervalInputModalProps> = props => {
    let generalPeriodInput = getGeneralPeriodInput(props.generalPeriod, props.subPeriod);
    return <Container>
        <Row className="align-items-center" form>
            <Col xs={{size: "auto"}}>
                Repeat every
            </Col>
            <Col xs={{size: 2}}>
                <Input type="number" name="periods"
                       min={1}
                       value={props.periods}
                       onChange={e => props.setPeriods(e.target.valueAsNumber)}/>
            </Col>
            <Col xs={{size: 3}}>
                <Input type="select" name="generalPeriod"
                       value={props.generalPeriod}
                       onChange={e => props.setGeneralPeriod(e.target.value as GeneralPeriod)}>
                    {Object.entries(displayGeneralPeriod)
                        .map(([period, display]) =>
                            <option value={period} key={period}>
                                {display}{props.periods === 1 ? "" : "s"}
                            </option>
                        )}
                </Input>
            </Col>
        </Row>
        {generalPeriodInput}
    </Container>;
};

export interface IntervalInputProps extends Omit<ButtonProps, "type"> {
    startDate: Dayjs | undefined

    interval?: RRule

    setInterval(schedule: RRule): void
}

export const IntervalInput: React.FC<IntervalInputProps> = ({startDate, interval, setInterval, ...props}) => {
    const [open, setOpen] = useState(false);
    const [generalPeriod, setGeneralPeriod] = useState(GeneralPeriod.WEEK);
    const [periods, setPeriods] = useState(1);
    const [weekdays, setWeekdays] = useState(new Set<WeekdayStr>());
    const [recurrenceDay, setRecurrenceDay] = useState<DayRecurrence>({
        type: DayRecurrenceType.MONTH_DAY,
        monthDay: {
            monthDay: 1,
        },
        nthWeekDay: {
            weekday: "SU",
            nth: 1
        }
    });
    const [month, setMonth] = useState(Months.JANUARY);
    const [day, setDay] = useState(1);

    function getRRule(): RRule {
        const options: Partial<Options> = {
            freq: GP_TO_RR_FREQUENCY[generalPeriod],
            interval: periods,
            dtstart: startDate?.toDate()
        };
        switch (generalPeriod) {
            case GeneralPeriod.DAY:
                break;
            case GeneralPeriod.WEEK:
                options.byweekday = Array.from(weekdays).map(wd => Weekday.fromStr(wd));
                break;
            case GeneralPeriod.MONTH:
                if (recurrenceDay.type === DayRecurrenceType.MONTH_DAY) {
                    const md = recurrenceDay.monthDay;
                    options.bymonthday = md.monthDay;
                } else {
                    const nd = recurrenceDay.nthWeekDay;
                    options.byweekday = RRule[nd.weekday].nth(nd.nth);
                }
                break;
            case GeneralPeriod.YEAR:
                options.bymonth = month.index;
                options.bymonthday = day;
                break;
            default:
                exhaustiveCheck(generalPeriod);
        }
        return new RRule(options);
    }

    function saveModalProps() {
        setInterval(getRRule());
    }

    const modalProps: IntervalInputModalProps = {
        startDate,
        generalPeriod, setGeneralPeriod, periods, setPeriods,
        subPeriod: {
            week: {
                weekdays, setWeekdays
            },
            month: {
                recurrenceDay, setRecurrenceDay
            },
            year: {
                month, setMonth, day, setDay
            },
        }
    };

    return <>
        <Button {...props} type="button"
                onClick={() => setOpen(true)}
                className={classNames(props.className, "form-control")}>
            Edit
        </Button>
        <SimpleModal title="Editing Interval"
                     submitLabel="Save"
                     size="lg"
                     isOpen={open}
                     closeModal={() => setOpen(false)}
                     onSubmit={saveModalProps}>
            <IntervalInputModal {...modalProps}/>
            <div className="ml-3 mt-3">
                Currently selected: <strong>{getRRule().toText()}</strong>
            </div>
        </SimpleModal>
    </>;
};
