package Comp3100Project;

public class ServerObj {
    private String type;
    private int id;
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

    public int getCore(){
        return this.core;
    }
}
