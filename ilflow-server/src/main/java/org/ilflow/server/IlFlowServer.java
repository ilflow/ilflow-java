package org.ilflow.server;

import org.ilflow.bridge.FunctionExit;
import org.ilflow.bridge.Utils;
import org.ilflow.compiler.ProjectCompiler;
import org.ilflow.compiler.TemplateCompiler;
import org.ilflow.model.Project;
import org.ilflow.repository.IlFlowRepositoryInterface;

import java.util.*;

public class IlFlowServer implements IlFlowServerInterface {
    private IlFlowRepositoryInterface repository;

    private static Random m_rand = new Random();

    private TreeMap<Integer, String> m_ProjectsByID = new TreeMap<>();
    private TreeMap<String, Integer> m_ProjectsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private TreeMap<Integer, ProjectRunner> m_Builds = new TreeMap<>();

    private TreeMap<Integer, ProjectRunner> m_Runners = new TreeMap<>();

    private TreeMap<Integer, Integer> m_ProjectsByPID = new TreeMap<>();
    private TreeMap<Integer, TreeMap<Integer, Boolean>> m_RunnersByID = new TreeMap<>();

    public IlFlowServer(IlFlowRepositoryInterface repository) {
        this.repository = repository;
    }

    private class EventPoint {
        Integer pid;
        Integer gid;
        HashMap data;
        FunctionExit port;
    }

    private TreeMap<Integer, EventPoint> m_RaisedEvents = new TreeMap<Integer, EventPoint>();
    private TreeMap<Integer, EventPoint> m_EventListeners = new TreeMap<Integer, EventPoint>();

    private TreeMap<Integer, Queue> m_EventResults = new TreeMap<Integer, Queue>();

    private TreeMap<Integer, Queue> m_ProcessResults = new TreeMap<Integer, Queue>();

    void callback(HashMap results) {
        Integer pid = (Integer) results.get("pid");
        if (!m_ProcessResults.containsKey(pid)) return;
        m_ProcessResults.put(pid, new LinkedList<HashMap>()).add(results);

        synchronized (m_ProcessResults.get(pid)) {
            m_ProcessResults.get(pid).notifyAll();
        }
        System.out.println("Signal reached to end node");
    }

    Integer eventqueue(HashMap data, FunctionExit port) {
        Integer returnValue = 0;

        EventPoint l = new EventPoint();
        l.data = data;
        l.port = port;

        String eventType = "raise";
        if (l.data.containsKey("systemEvent")) {
            eventType = l.data.get("systemEvent").toString();
            l.data.remove("systemEvent");
        }

        Integer pid = 0;
        if (l.data.containsKey("pid")) {
            pid = (Integer) l.data.get("pid");
            l.pid = pid;
            l.data.remove("pid");
        }

        Integer id = 0;
        if (l.data.containsKey("gid")) {
            id = (Integer) l.data.get("gid");
            l.gid = id;
            l.data.remove("gid");
        }

        synchronized (m_RaisedEvents) {
            if (eventType.equals("raise")) {
                Integer eid = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;
                while (m_RaisedEvents.containsKey(eid)) eid = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;
                m_RaisedEvents.put(eid, l);
                returnValue = eid;

                if (l.port == null) m_EventResults.put(eid, new LinkedList());

                for (Integer lid : m_EventListeners.keySet()) {
                    if (IsRespectiveListener(l, m_EventListeners.get(lid).data)) {
                        HashMap h = Utils.Clone(l.data);
                        h.put("systemEventId", eid);
                        m_EventListeners.get(lid).port.call(h);
                    }
                }

                if (m_Runners.containsKey(pid)) {
                    synchronized (m_Runners.get(pid)) {
                        m_Runners.get(pid).notifyAll();
                    }
                }
            } else if (eventType.equals("query")) {
                HashMap response = new HashMap();

                Integer i = 0;
                for (Integer eid : m_RaisedEvents.keySet()) {
                    if (m_RaisedEvents.get(eid).data.containsKey("system")) {
                        if (m_RaisedEvents.get(eid).data.get("system").toString().equals("EventTimeout")) {
//                            if (DateTime.FromBinary(Convert.ToInt64(m_RaisedEvents.put(eid].data.get("value"])).CompareTo(DateTime.Now) <= 0)
//                            {
//                                // TODO: remove key from m_RaisedEvents and m_EventResults tables
//                            }
                        }
                    }
                    if (IsRespectiveListener(m_RaisedEvents.get(eid), l.data)) response.put(i++, eid);
                }

                l.port.call(response);
            } else if (eventType.equals("fetch")) {
                HashMap response = new HashMap();
                Boolean success = true;

                if (l.data.containsKey("systemEventId")) {
                    Integer eventId = Integer.parseInt(l.data.get("systemEventId").toString());
                    if (!m_RaisedEvents.containsKey(eventId)) success = false;
                    else {
                        response = Utils.Clone(m_RaisedEvents.get(eventId).data);
                    }
                } else success = false;

                if (!success) response.put("success", false);

                l.port.call(response);
            } else if (eventType.equals("answer")) {
                HashMap response = new HashMap();
                Boolean success = true;

                if (l.data.containsKey("systemEventId")) {
                    Integer eventId = Integer.valueOf(l.data.get("systemEventId").toString());
                    l.data.remove("systemEventId");

                    if (!m_RaisedEvents.containsKey(eventId)) success = false;
                    else {
                        Boolean close = false;
                        if (l.data.containsKey("systemEventClose"))
                            close = Boolean.valueOf(l.data.get("systemEventClose").toString());

                        if (m_RaisedEvents.get(eventId).port != null)
                            m_RaisedEvents.get(eventId).port.call(l.data);
                        else {
                            m_EventResults.put(eventId, new LinkedList<HashMap>()).add(l.data);

                            synchronized (m_EventResults.get(eventId)) {
                                m_EventResults.get(eventId).notifyAll();
                            }
                        }

                        if (close) {
                            if (m_RaisedEvents.get(eventId).port != null) {
                                m_RaisedEvents.remove(eventId);
                            } else {
                                m_RaisedEvents.get(eventId).data.clear();
                                m_RaisedEvents.get(eventId).data.put("system", "EventTimeout");
                                m_RaisedEvents.get(eventId).data.put("value", 1);//DateTime.Now.putMinutes(5).ToBinary());
                            }
                        }
                    }
                } else success = false;

                response.put("success", success);

                l.port.call(response);
            } else if (eventType.equals("close")) {
                HashMap response = new HashMap();
                Boolean success = true;

                if (l.data.containsKey("systemEventId")) {
                    Integer eventId = Integer.parseInt(l.data.get("systemEventId").toString());
                    if (!m_RaisedEvents.containsKey(eventId)) success = false;
                    else {
                        if (m_RaisedEvents.get(eventId).port != null) {
                            m_RaisedEvents.remove(eventId);
                        } else {
                            m_RaisedEvents.get(eventId).data.clear();
                            m_RaisedEvents.get(eventId).data.put("system", "EventTimeout");
                            m_RaisedEvents.get(eventId).data.put("value", 1);//DateTime.Now.putMinutes(5).ToBinary());
                        }
                    }
                } else if (l.data.containsKey("systemListenerId")) {
                    Integer listenerId = Integer.parseInt(l.data.get("systemListenerId").toString());
                    if (!m_EventListeners.containsKey(listenerId)) success = false;
                    else {
                        m_EventListeners.remove(listenerId);
                    }
                } else success = false;

                response.put("success", success);

                l.port.call(response);
            } else if (eventType.equals("listen")) {
                Integer lid = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;
                while (m_EventListeners.containsKey(lid)) lid = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;
                m_EventListeners.put(lid, l);
                returnValue = lid;

                for (Integer eid : m_RaisedEvents.keySet()) {
                    if (IsRespectiveListener(m_RaisedEvents.get(eid), l.data)) {
                        HashMap h = Utils.Clone(m_RaisedEvents.get(eid).data);
                        h.put("systemEventId", eid);
                        l.port.call(h);
                    }
                }
            } else if (eventType.equals("systemLoad")) // internal, direct proccessing
            {
                if (l.data.containsKey("assembly")) l.data.remove("assembly");

                if (l.data.containsKey("projectName")) {
                    String projectName = l.data.get("projectName").toString();

                    long projectId;

                    if (!m_ProjectsByName.containsKey(projectName))
                        projectId = Load(projectName);
                    else
                        projectId = m_ProjectsByName.get(projectName);

                    synchronized (m_ProjectsByID) {
//                        if (m_Builds.containsKey(projectId))
//                            l.data.put("assembly", m_Builds.get(projectId).Assembly);
                    }
                }
            }
        }
        System.out.println("Node raised an event");

        return returnValue;
    }

    private Boolean IsRespectiveListener(EventPoint epoint, HashMap hlistener) {
        for (Object key : hlistener.keySet()) {
            if (!epoint.data.containsKey(key)) return false;
            if (!(epoint.data.get(key).toString().equals(hlistener.get(key).toString()))) return false;
        }
        return true;
    }

    public Integer[] GetLoadedProjectIDs() {
        return m_ProjectsByID.keySet().toArray(new Integer[m_ProjectsByID.size()]);
    }

    public String[] GetLoadedProjectNames() {
        return m_ProjectsByName.keySet().toArray(new String[m_ProjectsByName.size()]);
    }

    public Integer GetLoadedProjectID(String projectName) {
        if (!m_ProjectsByName.containsKey(projectName)) return 0;

        return m_ProjectsByName.get(projectName);
    }

    public String GetLoadedProjectSource(Integer id) {
        if (!m_ProjectsByID.containsKey(id)) return "";

        return ""; //Project.Serialize(m_Builds.get(id).Project);
    }

    private static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    public long GetProjectID(String projectName) {
        if (!m_ProjectsByName.containsKey(projectName))
            return Load(projectName);
        else
            return m_ProjectsByName.get(projectName);
    }

    // Returns ID
    public long Load(String projectName) {

        Project project = repository.GetProject(projectName);

        if (null == project) return 0;

        Integer id = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;

        Integer oldid = 0;

        synchronized (m_ProjectsByID) {
            while (m_ProjectsByID.containsKey(id)) id = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;

            TemplateCompiler templateCompiler = new TemplateCompiler();

            if(!templateCompiler.readTemplate()) return 0;

            ProjectCompiler compiler = new ProjectCompiler(templateCompiler);

            compiler.translate(project);

            String output = compiler.getOutput();

            System.out.println(output);
//            ProjectRunner runner;
//
//            try
//            {
//                runner = new ProjectCompiler().Compile(projectSource);
//            }
//            catch (Exception ex)
//            {
//                System.out.println(ex.Message);
//                return 0;
//            }
//
//            String projectName = runner.Project.Name;
//
//            m_Builds[id] = runner;
//            m_ProjectsByID[id] = projectName;
//
//            if (m_ProjectsByName.containsKey(projectName))
//            {
//                oldid = m_ProjectsByName[projectName];
//            }
//
//            m_ProjectsByName[projectName] = id;
        }

        if (oldid > 0) {
            Unload(oldid);
        }

        System.out.println("Project " + m_ProjectsByID.get(id) + " loaded with ID: " + id.toString());

        //SaveProject(m_ProjectsByID.get(id), projectSource);

        return id;
    }

    public String LoadErrors(String projectSource) {
        ProjectRunner runner;

//        try
//        {
//            runner = new ProjectCompiler().Compile(projectSource);
//        }
//        catch (Exception ex)
//        {
//            return ex.Message;
//        }

        return "";
    }

    public Boolean Delete(String projectName) {
        if (m_ProjectsByName.containsKey(projectName)) return false;

        //DeleteProject(projectName);

        return true;
    }

    public Boolean Unload(Integer id) {
        if (!m_ProjectsByID.containsKey(id)) return false;

        String name = m_ProjectsByID.get(id);

        synchronized (m_ProjectsByID) {
            // delete the project and stop its processes
            if (m_RunnersByID.containsKey(id)) {
//                List<Integer> toStop = new List<Integer>();
//
//                foreach (Integer pid in m_RunnersByID[id].Keys)
//                {
//                    toStop.put(pid);
//                }
//
//                foreach (Integer pid in toStop)
//                {
//                    if (!Stop(pid)) m_RunnersByID[id].remove(pid);
//                }

                m_RunnersByID.remove(id);
            }

            if (m_ProjectsByName.get(m_ProjectsByID.get(id)).equals(id))
                m_ProjectsByName.remove(m_ProjectsByID.get(id));

            m_ProjectsByID.remove(id);

            m_Builds.remove(id);
        }

        System.out.println("Project " + name + " unloaded");

        return true;
    }

    // Returns PID
    public Integer Run(Integer id) {
        Integer pid = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;

        synchronized (m_Runners) {
            if (!m_ProjectsByID.containsKey(id)) return 0;

//            ProjectRunner r = m_Builds[id].Copy();
//
//            while (m_Runners.containsKey(pid) || m_Builds.containsKey(pid)) pid = m_rand.nextInt(Integer.MAX_VALUE - 1) + 1;
//
//            m_Runners[pid] = r;
//
//            m_ProjectsByPID[pid] = id;
//
//            if (!m_RunnersByID.containsKey(id)) m_RunnersByID[id] = new TreeMap<Integer, Boolean>();
//            m_RunnersByID[id].put(pid, true);
//
//            if (!m_ProcessResults.containsKey(pid)) m_ProcessResults[pid] = new Queue();
//
//            r.SetPID(pid);
//            r.SetGID(id);
//
//            r.Run(callback, eventqueue);
        }

        System.out.println("Executed project with id: " + id.toString() + " and pid: " + pid.toString());

        return pid;
    }

    public Boolean Wait(Integer pid) {
        try {
            if (!m_Runners.containsKey(pid)) return false;

            synchronized (m_Runners.get(pid)) {
                m_Runners.get(pid).wait();
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public Boolean WaitResult(Integer pid) {
        try {
//            if (!m_ProcessResults.containsKey(pid)) return false;
//            synchronized (m_ProcessResults[pid])
//            {
//                if (m_ProcessResults[pid].size() > 0) return true;
//                Monitor.Wait(m_ProcessResults[pid]);
//            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public Boolean WaitEvent(Integer eid) {
        try {
//            if (!m_RaisedEvents.containsKey(eid)) return false;
//            if (!m_EventResults.containsKey(eid)) m_EventResults.put(eid] = new Queue();
//            synchronized (m_EventResults.put(eid])
//            {
//                Monitor.Wait(m_EventResults.put(eid]);
//            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public Integer RaiseEvent(HashMap h) {
        h.put("systemEvent", "raise");
        return eventqueue(h, null);
    }

    public Integer[] GetEventQueue(String[] eventKeyValues) {
        List<Integer> events = new ArrayList<Integer>();

//        HashMap h = StringArrayToHashtable(eventKeyValues);
//
//        synchronized (m_RaisedEvents)
//        {
//            foreach (Integer eid in m_RaisedEvents.Keys)
//            {
//                if (IsRespectiveListener(m_RaisedEvents.put(eid], h)) events.put(eid);
//            }
//        }

        return events.toArray(new Integer[events.size()]);
    }

    public HashMap GetEventData(Integer eid) {
        synchronized (m_RaisedEvents) {
            if (!m_RaisedEvents.containsKey(eid)) return null;

            return m_RaisedEvents.get(eid).data;
        }
    }

    public String GetEventDataBinary(Integer eid) {
        String result = null;

        synchronized (m_RaisedEvents) {
            if (!m_RaisedEvents.containsKey(eid)) return null;

//            MemoryStream msbin = new MemoryStream();
//            BinaryFormatter bf = new BinaryFormatter();
//            bf.Serialize(msbin, m_RaisedEvents.put(eid].data);
//
//            MemoryStream mszip = new MemoryStream();
//            GZipStream gz = new GZipStream(mszip, CompressionMode.Compress, false);
//            gz.Write(msbin.ToArray(), 0, (Integer)msbin.Length);
//
//            msbin = null;
//
//            result = Convert.ToBase64String(mszip.ToArray());
//
//            mszip = null;
        }

        return result;
    }

    public Boolean SetEventResponse(Integer eid, HashMap responseKeyValues) {
        synchronized (m_RaisedEvents) {
            if (!m_RaisedEvents.containsKey(eid)) return false;

            m_RaisedEvents.get(eid).port.call(responseKeyValues);
            m_RaisedEvents.remove(eid);
        }

        return true;
    }

    public Integer GetResultsCount(Integer pid) {
        if (m_ProcessResults.containsKey(pid)) return m_ProcessResults.get(pid).size();
        return 0;
    }

    public HashMap GetNextResult(Integer pid) {
        if (!m_ProcessResults.containsKey(pid)) return null;
        if (m_ProcessResults.get(pid).size() < 1) return null;

        HashMap sresult = (HashMap) m_ProcessResults.get(pid).poll();

        return sresult;
    }

    public Integer GetEventResultsCount(Integer eid) {
        if (m_EventResults.containsKey(eid)) return m_EventResults.get(eid).size();
        return 0;
    }

    public HashMap GetEventNextResult(Integer eid) {
        if (!m_EventResults.containsKey(eid)) return null;
        if (m_EventResults.get(eid).size() < 1) return null;

        HashMap sresult = (HashMap) m_EventResults.get(eid).poll();

        return sresult;
    }

    public Boolean IsRunning(Integer pid) {
        try {
            if (!m_Runners.containsKey(pid)) return false;
            return m_Runners.get(pid).IsRunning();
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean Stop(Integer pid) {
        synchronized (m_Runners) {
            if (!m_Runners.containsKey(pid)) return false;
//            m_Runners[pid].Kill();
//
//            m_RunnersByID[m_ProjectsByPID[pid]].remove(pid);
//            m_ProjectsByPID.remove(pid);
//
//            synchronized (m_Runners[pid])
//            {
//                Monitor.PulseAll(m_Runners[pid]);
//            }
//            m_Runners.remove(pid);
//
//            synchronized (m_ProcessResults[pid])
//            {
//                Monitor.PulseAll(m_ProcessResults[pid]);
//            }
            m_ProcessResults.remove(pid);
        }

        System.out.println("Stopped running project instance with pid: " + pid.toString());

        synchronized (m_RaisedEvents) {
//            List<Integer> listenersToStop = new List<Integer>();
//
//            foreach (Integer eid in m_EventListeners.Keys)
//            {
//                if (m_EventListeners[eid].pid == pid)
//                    listenersToStop.put(eid);
//            }
//
//            foreach (Integer eid in listenersToStop)
//            {
//                m_EventListeners.remove(eid);
//            }
//
//            System.out.println("Stopped " + listenersToStop.size().toString() + " listeners");
//
//            List<Integer> eventsToClose = new List<Integer>();
//
//            foreach (Integer eid in m_RaisedEvents.Keys)
//            {
//                if (m_RaisedEvents.put(eid].pid == pid)
//                    eventsToClose.put(eid);
//            }
//
//            foreach (Integer eid in eventsToClose)
//            {
//                m_RaisedEvents.remove(eid);
//            }
//
//            System.out.println("Closed " + eventsToClose.size().toString() + " raised events");
        }

        return true;
    }

}
