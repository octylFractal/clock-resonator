import classNames from "classnames";
import React, {MouseEvent} from "react";

interface TextCheckBaseProps {
    shortLabel: string
    longLabel: string
    checked: boolean
    rounded?: boolean

    setChecked(checked: boolean): void
}

export type TextCheckBoxProps = TextCheckBaseProps ;

export const TextCheckBox: React.FC<TextCheckBoxProps> = (
    {shortLabel, longLabel, checked, rounded, setChecked}
) => {
    const cnames = classNames("text-check-box", {
        checked: checked,
        round: rounded
    });

    function toggle(e: MouseEvent<HTMLDivElement>) {
        e.preventDefault();
        setChecked(!checked);
    }

    return <div role="checkbox" aria-label={longLabel} aria-checked={checked}
                className={cnames} onClick={toggle}>
        {shortLabel}
    </div>;
};