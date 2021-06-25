import React from "react";
import {CardDeck} from "reactstrap";
import {TimeCardEntry} from "../data/TimeCardEntry";
import {TimeCard} from "./TimeCard";

export interface TimeCardsProps {
    entries: TimeCardEntry[]
}

export const TimeCards: React.FC<TimeCardsProps> = ({entries}) => {
    return <CardDeck>
        {entries.map(entry =>
            <TimeCard entry={entry} key={entry.id}/>
        )}
    </CardDeck>;
};
