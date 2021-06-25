import React, {useState} from "react";
import {hot} from "react-hot-loader/root";
import {Form, FormGroup, Input, InputGroup, InputGroupAddon, Label} from "reactstrap";
import {RRule} from "rrule";
import {IntervalInput} from "../interval/IntervalInput";

const AddNewEntry: React.FC = () => {
    const [name, setName] = useState<string>("");
    const [date, setDate] = useState<string>("");
    const [interval, setInterval] = useState<RRule>();

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
                    }/>
                    <InputGroupAddon addonType="append">
                        <IntervalInput name="interval" id="ane-interval"
                                       color="info"
                                       interval={interval}
                                       setInterval={setInterval}/>
                    </InputGroupAddon>
                </InputGroup>
            </FormGroup>
        </Form>
    </div>;
};

export default hot(AddNewEntry);
