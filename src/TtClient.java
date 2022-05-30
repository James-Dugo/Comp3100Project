import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TtClient extends Client{

    private ServerObj serverStar=null;
    private int size;
    private int minSize;

    /**
     * Constructor method that sets the hostname and port
     */
    TtClient(String hostname, int port, Boolean verbose,int size,int minSize){
        this.hostname=hostname;
        this.port=port;
        this.verbose=verbose;
        this.size=size;
        this.minSize=minSize;
    }

    /**
     * 
     */
    public void runClient(){
        this.newConn(hostname, port);
        
        loop:
        while(true){
            this.send("REDY\n");
            this.splitReply();

            switch(this.splitReply[0]){
                case "JOBN":
                    //The current job is this reply
                    this.currentJob=new JobObj(this.reply);
                    //GETS Capable currentJob
                    this.getCapable();
                    this.sortBySize(this.serverList);
                    this.serverStar=this.getStar();
                    this.schd(this.currentJob.getID(), this.serverStar.getType(), this.serverStar.getId());
                    continue loop;

                case "JCPL":
                    int overSize=this.size;
                    int underSize=this.minSize;
                    this.getAll();
                    List<ServerObj> idleList = new ArrayList<ServerObj>();
                    List<ServerObj> overList = new ArrayList<ServerObj>();
                    for(ServerObj server: this.serverList){
                        if(server.getWJobs()>overSize){
                            this.setJobs(server);
                        }
                        if(server.getWJobs()<underSize){
                            idleList.add(server);
                        }
                        if(server.getWJobs()>2){
                            overList.add(server);
                        }
                    }

                    for(ServerObj overServer:overList){
                        while(overServer.jobs.size()>overSize&&idleList.size()>0){
                            if(verbose){System.out.println(String.format("Migrating from: %s %d, to: %s %d",overServer.getType(),overServer.getId(),idleList.get(0).getType(),idleList.get(0).getId()));}
                            this.migrate( (overServer.jobs.get(overServer.jobs.size()-1)), overServer, idleList.get(0));
                            idleList.remove(0);
                        }
                    }
                    continue loop;

                case "NONE":
                    this.cleanup();
                    break loop;
                default:
                    continue loop;
            }
        }

    }

    private void migrate(JobObj job, ServerObj source,ServerObj target) {
        this.send(String.format("MIGJ %d %s %d %s %d\n",job.getID(),source.getType(),source.getId(),target.getType(),target.getId()));
    }

    private void getAll() {
        this.serverList.clear();
        this.send("GETS All\n");
        this.splitReply();
        int numLines=Integer.parseInt(this.splitReply[1]);
        this.write("OK\n");
        for (int i = 0; i < numLines; i++) {
            this.reply=this.recieve();
            this.serverList.add(new ServerObj(this.reply));
        }
        this.send("OK\n");
    }

    private void setJobs(ServerObj server) {
        this.send(String.format("LSTJ %s %d\n",server.getType(),server.getId()));
        this.splitReply();
        int numLines=Integer.parseInt(this.splitReply[1]);
        this.write("OK\n");
        for(int i=0;i<numLines;i++){
            this.reply=this.recieve();
            this.splitReply();
            server.jobs.add(new JobObj(this.splitReply));
        }
        this.send("OK\n");
    }

    /**
     * finds the best server in this.serverList based on a set of conditions 
     * 1. The server is "idle"
     * 2. The server has no waiting jobs
     * 3. The server is active||booting
     * @return
     */
    private ServerObj getStar() {

        for(ServerObj server:this.serverList){
            if(server.getStatus().equals("idle")){return server;}

            if(server.getWJobs()==0){
                return server;
            }
        }

        for(ServerObj server:this.serverList){
            if(server.getStatus().equals("active")||server.getStatus().equals("booting")){
                return server;
            }
        }

        return this.serverList.get(0);
    }

    /**
     * Given a List of server objects this function sorts them by:
     * 1-5 lexicographically
     * 1.Cores asc
     * 2.Memory asc
     * 3.Disk asc
     * 4.Waiting Jobs desc
     * 5.Running Jobs desc
     */
    private void sortBySize(List<ServerObj> list) {
        Collections.sort(list, new Comparator<ServerObj>(){
            @Override
            public int compare(ServerObj a, ServerObj b){
                //By ascending Core size>Memory>Disk
                int coreDiff=a.getCore()-b.getCore();
                int memDiff=a.getMem()-b.getMem();
                int diskDiff=a.getDisk()-b.getDisk();

                //Then By descending Waiting Jobs>Running Jobs
                int wJobDiff=b.getWJobs()-a.getWJobs();
                int rJobDiff=b.getRJobs()-a.getRJobs();
        
                if(coreDiff!=0){return coreDiff;}
                else if(memDiff!=0){return memDiff;}
                else if(diskDiff!=0){return diskDiff;}
                else if(wJobDiff!=0){return wJobDiff;}
                else{return rJobDiff;}
            }
        });
    }

}
