import { ParamMap } from '@angular/router';

export function getSessionNamesMap(queryParamMap: ParamMap): Map<string, string> {
    let sessions = queryParamMap.getAll('session');
    let sessionNames = queryParamMap.getAll('sessionName');

    if (!sessions || sessions.length == 0) {
        sessions = ['default'];
    }

    if (!sessionNames) {
        sessionNames = [];
    }

    const sessionMap: Map<string, string> = new Map();

    for (let i = 0; i < sessions.length; i++) {

        let sessionName = sessions[i];
        if (sessionNames.length > i) {
            sessionName = sessionNames[i];
        }

        sessionMap.set(sessions[i], sessionName);
    }
    
    return sessionMap;
}