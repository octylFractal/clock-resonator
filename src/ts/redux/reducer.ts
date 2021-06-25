import {combineReducers, createSlice, PayloadAction} from "@reduxjs/toolkit";
import {Draft} from "immer";
import {TimeCardEntry} from "../data/TimeCardEntry";

const {actions: timeCardEntriesActions, reducer: timeCardEntries} = createSlice({
    name: 'timeCardEntries',
    initialState: new Array<TimeCardEntry>(),
    reducers: {
        set: {
            reducer(state, action: PayloadAction<TimeCardEntry>) {
                state.push(action.payload)
            },
            prepare(value: TimeCardEntry) {
                return {payload: value}
            }
        }
    }
});

export {timeCardEntriesActions as timeCardEntries};

export interface UserInfoRecord {
    readonly isKnown: boolean
    readonly name?: string
    readonly uid?: string
}

export interface UserInfo {
    readonly name: string
    readonly uid: string
}

const {actions: userInfoActions, reducer: userInfo} = createSlice({
    name: 'userInfo',
    initialState: {
        isKnown: false
    },
    reducers: {
        login: {
            reducer(state: Draft<UserInfoRecord>, action: PayloadAction<UserInfo>) {
                return {isKnown: true, ...action.payload};
            },
            prepare(value: UserInfo) {
                return {payload: value}
            }
        },
        logout() {
            return {isKnown: true};
        }
    }
});

export {userInfoActions as userInfo};

export const reducer = combineReducers({
    timeCardEntries, userInfo
});