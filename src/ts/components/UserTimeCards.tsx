import {hot} from "react-hot-loader/root";
import {connect} from "react-redux";
import {LocalState} from "../redux/store";
import {TimeCards} from "./TimeCards";

const UserTimeCards = connect((state: LocalState) => ({
    entries: state.timeCardEntries
}))(TimeCards);

export default hot(UserTimeCards);
