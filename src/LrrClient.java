
public class LrrClient extends Client{

    ServerObj largest;

    /**
     * checks through the list of servers and sets this.largest
     * to the server with the most cores
     */
    void pickLargest() {
        this.largest=this.serverList.get(0);
        for (ServerObj server : this.serverList) {
            if(server.getCore()>this.largest.getCore()){
                this.largest=server;
            }
        }
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
                if(currentServerId>this.largest.getLimit()){
                    currentServerId=0;
                }else{
                    currentServerId++;
                }
                firstLoop=false;
                continue loop;
            }else{
                this.send("REDY\n");
                this.splitReply();
                swizzle:
                switch(this.splitReply[0]){
                    case("JOBN"):
                        this.currentJob=new JobObj(this.reply);
                        this.getCapable();
                        schd(currentServerId);
                        if(currentServerId>this.largest.getLimit()){
                            currentServerId=0;
                        }else{
                            currentServerId++;
                        }
                        break swizzle;
                    case("NONE"): break loop;
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