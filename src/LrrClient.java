
public class LrrClient extends Client{

    ServerObj largest;

    
    /**
     * Constructor method that sets the hostname and port
     */
    LrrClient(String host,int port){
        this.hostname=host;
        this.port=port;    
    }
    
    /**
     * checks through the list of servers and sets this.largest
     * to the server with the most cores, it then sets the "limit"
     * or number of that type of server for iterating through later
     */
    void pickLargest() {
        this.largest=this.serverList.get(0);
        for (ServerObj server : this.serverList) {
            if(server.getCore()>this.largest.getCore()){
                this.largest=server;
            }
        }

        for (ServerObj server : this.serverList){
            if(server.getType().equals(this.largest.getType())){
                this.largest.incrementLimit();
            }
        }
        this.largest.decrLimit();
    }

    void mainLoop(){
        Boolean firstLoop=true;
        int currentServerId=0;
        this.newConn("localhost", 50000);

        loop:
        while(true){
            //Assuming the first loop reply is a JOBN and that
            //no GETS will add a new largest server
            if(firstLoop){
                this.send("REDY\n");
                this.currentJob=new JobObj(this.reply);
                this.getCapable();
                this.pickLargest();
                schd(currentServerId);
                if(currentServerId<this.largest.getLimit()){
                    currentServerId++;
                }else{
                    currentServerId=0;
                }
                firstLoop=false;
                continue loop;
            }else{
                this.send("REDY\n");
                this.splitReply();
                swizzle:
                switch(this.splitReply[0]){
                    case "JOBN":
                        this.currentJob=new JobObj(this.reply);
                        this.getCapable();
                        schd(currentServerId);
                        if(currentServerId<this.largest.getLimit()){
                            currentServerId++;
                        }else{
                            currentServerId=0;
                        }
                        break swizzle;
                    case "NONE": break loop;
                    default: break swizzle;
                }
            }
        }
        this.cleanup();
    }

    /**
     * schedules this.jobId to this.largest.getType(), serverid 
     * @param int serverid
     */
    void schd(int serverid) {
        this.send(String.format("SCHD %d %s %d\n",this.currentJob.getID(),this.largest.getType(),serverid));
    }
}