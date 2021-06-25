import dayjs from "dayjs";
import React, {useMemo, useState} from "react";
import {hot} from "react-hot-loader/root";
import {Form, FormFeedback, FormGroup, Input, InputGroup, InputGroupAddon, Label} from "reactstrap";
import {RRule} from "rrule";
import {IntervalInput} from "../interval/IntervalInput";

const AddNewEntry: React.FC = () => {
    const [name, setName] = useState<string>("");
    const [date, setDate] = useState<string>("");
    const [interval, setInterval] = useState<RRule>();

    const dateObj = useMemo(() => {
        if (date === "") {
            return undefined;
        }
        return dayjs(date);
    }, [date]);
    const isValidInterval = useMemo(() => {
        if (typeof interval === "undefined" || typeof dateObj === "undefined") {
            return undefined;
        }
        return dateObj.isSame(interval.after(dateObj.toDate(), true), 'day')
    }, [dateObj, interval]);

    return <div className="w-50">
        <Form>
            <FormGroup>
                <Label for="ane-name">Name</Label>
                <Input type="text" name="name" id="ane-name"
                       value={name}
                       onChange={e => setName(e.target.value)}/>
            </FormGroup>
            <FormGroup>
                <Label for="ane-date">Date</Label>
                <Input type="date" name="date" id="ane-date"
                       value={date}
                       onChange={e => setDate(e.target.value)}/>
            </FormGroup>
            <FormGroup>
                <Label for="ane-interval">Interval</Label>
                <InputGroup>
                    <Input type="text" readOnly value={
                        interval ? interval.toText() : ""
                    } invalid={isValidInterval === false} valid={isValidInterval}/>
                    <InputGroupAddon addonType="append">
                        <IntervalInput name="interval" id="ane-interval"
                                       color="info"
                                       startDate={dateObj}
                                       interval={interval}
                                       setInterval={setInterval}/>
                    </InputGroupAddon>
                    <FormFeedback>The date {date} is not part of the interval!</FormFeedback>
                </InputGroup>
            </FormGroup>
        </Form>
    </div>;
};

export default hot(AddNewEntry);
