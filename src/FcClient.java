public class FcClient extends Client{

    /**
     * Constructor method that sets the hostname and port
     */
    FcClient(String hostname, int port, Boolean verbose){
        this.hostname=hostname;
        this.port=port;
        this.verbose=verbose;
    }

    ServerObj first;

    void runClient(){
        this.newConn(this.hostname,this.port);

        loop:
        while(true){
            this.send("REDY\n");
            this.splitReply();
            switch(this.splitReply[0]){
                case "JOBN":
                    this.currentJob=new JobObj(this.reply);
                    this.getCapable();
                    this.first=this.serverList.get(0);
                    this.schd(this.currentJob.getID(),this.first.getType(),0);
                    continue loop;
                case "NONE":
                    this.cleanup();
                    break loop;
                default:
                    continue loop;
            }
        }
        
    }
}
