import React from "react";
import {Col, Container, Input, Row} from "reactstrap";
import {Month, ORDERED_MONTHS} from "../../data/time";

export interface YearIntervalInputProps {
    month: Month

    setMonth(month: Month): void

    day: number

    setDay(day: number): void
}

export const YearIntervalInput: React.FC<YearIntervalInputProps> = (
    {month, setMonth, day, setDay}
) => {
    return <Container className="py-3">
        <Row form className="align-items-center">
            <Col>
                Month:
                <Input className="inline" type="select"
                       value={month.index}
                       onChange={e => {
                           const newMonth = ORDERED_MONTHS[(+e.target.value) - 1];
                           setMonth(newMonth);
                           setDay(Math.min(day, newMonth.maximumDays));
                       }}>
                    {ORDERED_MONTHS.map(month => {
                        return <option value={month.index} key={month.index}>{month.name}</option>;
                    })}
                </Input>
            </Col>
        </Row>
        <Row>
            <Col>
                Day of Month:
                <Input className="inline" type="number" min={1} max={month.maximumDays}
                       value={day}
                       onChange={e => setDay(e.target.valueAsNumber)}>
                </Input>
            </Col>
        </Row>
    </Container>;
};
