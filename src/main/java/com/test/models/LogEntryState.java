package com.test.models;

public enum LogEntryState {

    STARTED, FINISHED;

    public static LogEntryState getByName(String name){
        for (LogEntryState state: LogEntryState.values()) {
            if(state.name().equals(name)){
                return state;
            }
        }
        return null;
    }
}
