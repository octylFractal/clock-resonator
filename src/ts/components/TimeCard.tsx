import {faCheckCircle, faClock, faTimesCircle} from "@fortawesome/free-regular-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import React, {useEffect, useState} from "react";
import {Button, Card, CardBody, CardFooter, CardHeader, Progress} from "reactstrap";
import {TimeCardEntry} from "../data/TimeCardEntry";
import {prettyDate} from "../dates";
import {PartialProgress} from "./PartialProgress";

export interface TimeCardProps {
    entry: TimeCardEntry
}

interface PercentProps {
    percentage: number
}

const TimeCardText: React.FC<TimeCardProps & PercentProps> = ({entry, percentage}) => {
    return <div className="pb-3" style={{overflowX: "auto"}}>
        <FontAwesomeIcon icon={faCheckCircle} className="text-success"/>{" "}
        <strong>Last completion</strong>{" " + prettyDate(entry.lastCompleteTime)}.
        <br/>
        <ExpectedProgressIcon percentage={percentage}/>
        {" "}
        <strong>Expected next completion</strong>
        {" " + prettyDate(entry.expectedCompletionTime)}.
        <br/>
        <FontAwesomeIcon icon={faClock}/>
        {" "}
        <strong>Interval</strong>: {entry.interval.toText()}
    </div>;
};

const ExpectedProgressIcon: React.FC<PercentProps> = ({percentage}) => {
    if (percentage < 80) {
        return <FontAwesomeIcon title="Plenty of time!" icon={faClock} className="text-success"/>;
    }
    if (percentage < 100) {
        return <FontAwesomeIcon title="Almost passed!" icon={faClock} className="text-warning"/>;
    }
    return <FontAwesomeIcon title="Passed! Oh no!" icon={faTimesCircle} className="text-danger"/>;
};

export const TimeCard: React.FC<TimeCardProps> = ({entry}) => {
    function computePercentage() {
        return entry.percentComplete;
    }

    const [percentage, setPercentage] = useState(computePercentage);

    function updateRing() {
        setPercentage(computePercentage());
    }

    function resetRing() {

    }

    useEffect(() => {
        const internalId = setInterval(updateRing, 50);
        return () => {
            clearInterval(internalId);
        };
    });

    return <Card>
        <CardHeader style={{fontSize: "1.5rem"}}>{entry.name}</CardHeader>
        <CardBody>
            <TimeCardText entry={entry} percentage={percentage}/>
            <div className="d-flex justify-content-end">
                <Button color="primary" onClick={() => resetRing()}>Complete</Button>
            </div>
        </CardBody>
        <CardFooter className="p-0">
            <Progress style={{backgroundColor: "unset"}} multi>
                <PartialProgress percentage={percentage} start={0} cap={80} color="success"/>
                <PartialProgress percentage={percentage} start={80} cap={95} color="warning"/>
                <PartialProgress percentage={percentage} start={95} cap={100} color="danger"/>
            </Progress>
        </CardFooter>
    </Card>;
};