package Comp3100Project;

import org.w3c.dom.Element;

public class ServerObj {
    private String type;
    private int id;
    private int limit;
    private String status;
    private int currentStartTime;
    private int core;
    private int mem;
    private int disk;

    public ServerObj(String reply){
        String[] serverArr=reply.split(" ");
        type=serverArr[0];
        id=Integer.parseInt(serverArr[1]);
        status=serverArr[2];
        currentStartTime=Integer.parseInt(serverArr[3]);
        core=Integer.parseInt(serverArr[4]);
        mem=Integer.parseInt(serverArr[5]);
        disk=Integer.parseInt(serverArr[6]);
    }
    // format for reference <server type="juju" limit="2" bootupTime="60" hourlyRate="0.20" cores="2" memory="4000" disk="16000" />
    public ServerObj(Element server){
        type=server.getAttribute("type");
        limit=Integer.parseInt(server.getAttribute("limit"));
        core=Integer.parseInt(server.getAttribute("cores"));
        mem=Integer.parseInt(server.getAttribute("memory"));
        disk=Integer.parseInt(server.getAttribute("disk"));
    }

    public int getCore(){
        return this.core;
    }
}
