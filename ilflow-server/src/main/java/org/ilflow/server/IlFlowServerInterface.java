package org.ilflow.server;

import java.util.HashMap;

public interface IlFlowServerInterface {
    Integer[] GetLoadedProjectIDs();

    String[] GetLoadedProjectNames();

    Integer GetLoadedProjectID(String projectName);

    String GetLoadedProjectSource(Integer id);

    long GetProjectID(String projectName);

    // Returns ID
    long Load(String projectName);

    String LoadErrors(String projectSource);

    Boolean Unload(Integer id);

    // Returns PID
    Integer Run(Integer id);

    Boolean Wait(Integer pid);

    Boolean WaitResult(Integer pid);

    Boolean WaitEvent(Integer eid);

    Integer RaiseEvent(HashMap h);

    Integer[] GetEventQueue(String[] eventKeyValues);

//    HashMap GetEventData(Integer eid);

    String GetEventDataBinary(Integer eid);

    Boolean SetEventResponse(Integer eid, HashMap responseKeyValues);

    Integer GetResultsCount(Integer pid);

//    HashMap GetNextResult(Integer pid);

    Integer GetEventResultsCount(Integer eid);

//    HashMap GetEventNextResult(Integer eid);

    Boolean IsRunning(Integer pid);

    Boolean Stop(Integer pid);
}
