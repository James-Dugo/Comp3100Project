import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;

public class TtClient extends Client{

    private ServerObj serverStar=null;

    /**
     * Constructor method that sets the hostname and port
     */
    TtClient(String hostname, int port, Boolean verbose){
        this.hostname=hostname;
        this.port=port;
        this.verbose=verbose;
    }

    /**
     * I want to prioritise in order
     * 1. Servers that are Idle waiting for a Job
     * 2. The server that best fits the job (get servers capable, sort by size, pick first)
     * 3. I don't need to parralellize the jobs since the time to complete doesn't take into account free space
     * 
     * Step 1. get a list of the servers, The xml is cleanest for getting the list of servers.
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
                    //For each server get the jobs and attach them to the server if there are any
                    for(ServerObj server:this.serverList){
                        if( server.getWJobs()>0 || server.getRJobs()>0){
                            this.getJobs(server);
                        }                        
                    }

                    this.sortBySize();
                    this.serverStar=this.getStar();
                    this.schd(this.currentJob.getID(), this.serverStar.getType(), this.serverStar.getId());
                    continue loop;
                case "NONE":
                    this.cleanup();
                    break loop;
                default:
                    continue loop;
            }
        }

    }

    private void getJobs(ServerObj server) {
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

    private ServerObj getStar() {
        int min=Integer.MAX_VALUE;
        ServerObj temp=null;

        for(ServerObj server:this.serverList){
            if(server.getWJobs()>0||server.getRJobs()>0){
                if(server.checkAvailableResources(this.currentJob)){
                    return server;
                }
            }
        }

        for(ServerObj server:this.serverList){
            if(server.getWJobs()==0 && server.getRJobs()==0){
                return server;}
        }
        for(ServerObj server:this.serverList){
            if(server.getWJobs()==0){
                return server;}
        }
        for(ServerObj server:this.serverList){
            if( server.getWJobs()<min && (server.getStatus().equals("booting")||server.getStatus().equals("active")) ){
                min=server.getWJobs();
                temp=server;
            }
        }

        if(temp==null){return this.serverList.get(0);}

        return temp;
    }

    private void sortBySize() {
        Collections.sort(this.serverList, new Comparator<ServerObj>(){
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
