import {configureStore, Store} from "@reduxjs/toolkit";
import {reducer} from "./reducer";

export const store = configureStore({
    reducer
});

type StoreToState<T extends Store<any>> = T extends Store<infer R> ? R : never;

export type LocalState = StoreToState<typeof store>;
