import dayjs from "dayjs";
import isSameOrAfter from "dayjs/plugin/isSameOrAfter";
import utc from "dayjs/plugin/utc";
import React, {useMemo, useState} from "react";
import {hot} from "react-hot-loader/root";
import {Form, FormFeedback, FormGroup, Input, InputGroup, Label} from "reactstrap";
import {RRule} from "rrule";
import {computeInterval} from "../../data/interval";

dayjs.extend(utc);
dayjs.extend(isSameOrAfter);

interface IntervalErrorResult {
    startError?: string
    nextError?: string
}

type IntervalResult = RRule | IntervalErrorResult;

const AddNewEntry: React.FC = () => {
    const [name, setName] = useState<string>("");
    const now = dayjs().startOf("day");
    const [startDate, setStartDate] = useState<string>("");
    const [nextDate, setNextDate] = useState<string>("");
    const interval: IntervalResult = useMemo(() => {
        if (startDate === "" || nextDate === "") {
            return {};
        }
        const startUtc = dayjs(startDate).utc();
        const nextUtc = dayjs(nextDate).utc();
        if (startUtc.isSameOrAfter(nextUtc)) {
            return {
                startError: "Start date must be before next date",
                nextError: "Next date must be after start date"
            };
        }
        if (startUtc.isBefore(now)) {
            return {startError: "Start date must be the same as or after today's date"};
        }
        if (nextUtc.isBefore(now)) {
            return {nextError: "Next date must be the same as or after today's date"};
        }
        return computeInterval(startUtc, nextUtc);
    }, [startDate, nextDate]);

    return <div className="w-50">
        <Form>
            <FormGroup>
                <Label for="ane-name">Name</Label>
                <Input type="text" name="name" id="ane-name"
                       value={name}
                       onChange={e => setName(e.target.value)}/>
            </FormGroup>
            <FormGroup>
                <Label for="ane-start-date">Start Date</Label>
                <Input type="date" name="start-date" id="ane-start-date"
                       value={startDate}
                       min={dayjs().format("YYYY-MM-DD")}
                       max={nextDate !== "" ? nextDate : undefined}
                       onChange={e => setStartDate(e.target.value)}
                       invalid={!(interval instanceof RRule) && !!interval.startError}/>
                <FormFeedback>{interval instanceof RRule ? "" : interval.startError}</FormFeedback>
            </FormGroup>
            <FormGroup>
                <Label for="ane-next-date">Next Date</Label>
                <Input type="date" name="next-date" id="ane-next-date"
                       value={nextDate}
                       min={startDate !== "" ? startDate : dayjs().format("YYYY-MM-DD")}
                       onChange={e => setNextDate(e.target.value)}
                       invalid={!(interval instanceof RRule) && !!interval.nextError}/>
                <FormFeedback>{interval instanceof RRule ? "" : interval.nextError}</FormFeedback>
            </FormGroup>
            <FormGroup>
                <Label for="ane-interval">Interval</Label>
                <InputGroup>
                    <Input type="text" readOnly value={
                        interval instanceof RRule ? interval.toText() : ""
                    }/>
                </InputGroup>
            </FormGroup>
        </Form>
    </div>;
};

export default hot(AddNewEntry);
