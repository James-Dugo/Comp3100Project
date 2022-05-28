

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;

public class ServerObj {
    private String type;
    private int id;
    private int limit=0;
    private int bootupTime;
    private String status;
    private int currentStartTime;
    private int core;
    private int mem;
    private int disk;
    private int availableCore;
    private int availableMem;
    private int availableDisk;
    private int wJobs;
    private int rJobs;
    private int fitness;

    public List<JobObj> jobs=new ArrayList<JobObj>();

    public ServerObj(String reply){
        String[] serverArr=reply.split(" ");
        type=serverArr[0];
        id=Integer.parseInt(serverArr[1]);
        status=serverArr[2];
        currentStartTime=Integer.parseInt(serverArr[3]);
        core=Integer.parseInt(serverArr[4]);
        mem=Integer.parseInt(serverArr[5]);
        disk=Integer.parseInt(serverArr[6]);
        if(serverArr.length>6){
            wJobs=Integer.parseInt(serverArr[7]);
            rJobs=Integer.parseInt(serverArr[8]);
        }
    }
    // format for reference <server type="juju" limit="2" bootupTime="60" hourlyRate="0.20" cores="2" memory="4000" disk="16000" />
    public ServerObj(NamedNodeMap server){
        type=server.getNamedItem("type").getTextContent();
        limit=Integer.parseInt(server.getNamedItem("limit").getTextContent());
        bootupTime=Integer.parseInt(server.getNamedItem("bootupTime").getTextContent());
        core=Integer.parseInt(server.getNamedItem("cores").getTextContent());
        mem=Integer.parseInt(server.getNamedItem("memory").getTextContent());
        disk=Integer.parseInt(server.getNamedItem("disk").getTextContent());
    }

    /**
     * Given a Job checks that the list of jobs waiting on the server currently leave room for that job to be run immediately
     * @param currJob
     * @return true if the currJob can be run immediately false otherwise
     */
    public Boolean checkAvailableResources(JobObj currJob){
        this.resetAvailable();

        for (JobObj job : this.jobs) {
            this.availableCore-=job.getCores();
            this.availableMem-=job.getMem();
            this.availableDisk-=job.getDisk();
        }
        if(this.core<currJob.getCores()){return false;}
        else if(this.mem<currJob.getMem()){return false;}
        else if(this.disk<currJob.getDisk()){return false;}
        else{return true;}
    }

    public int getFitnessScore(){
        this.resetAvailable();

        for (JobObj job : this.jobs){
            this.availableCore-=job.getCores();
        }

        this.fitness=this.core-this.availableCore;
        return this.fitness;
    }

    private void resetAvailable() {
        this.availableCore=this.core;
        this.availableMem=this.mem;
        this.availableDisk=this.disk;
    }
    public void setAvailable() {
        this.core=this.availableCore;
        this.mem=this.availableMem;
        this.disk=this.availableDisk;
    }

    public int    getCore()   {return this.core;}
    public int    getLimit()  {return this.limit;}
    public int    getMem()    {return this.mem;}
    public int    getDisk()   {return this.disk;}
    public int    getId()     {return this.id;}
    public int    getWJobs()  {return this.wJobs;}
    public int    getRJobs()  {return this.rJobs;}
    public String getType()   {return this.type;}
    public String getStatus() {return this.status;}

    public void incrementLimit() {this.limit++;}
    public void decrLimit()      {this.limit--;}


}
