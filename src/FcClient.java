import java.util.logging.Level;

public class FcClient extends Client{

    /**
     * Constructor method that sets the hostname and port
     */
    FcClient(String hostname, int port){
        this.hostname=hostname;
        this.port=port;
    }

    ServerObj first;

    void mainLoop(){
        this.newConn(this.hostname,this.port);

        loop:
        while(true){
            this.send("REDY\n");
            switch(this.reply){
                case "JOBN":
                    this.getCapable();
                    this.first=this.serverList.get(0);
                    this.schd(this.currentJob.getID(),this.first.getType(),0);
                    continue loop;
                case "NONE":
                    this.cleanup();
                    break loop;
                default:
                    this.logger.log(Level.INFO,"Reached default switch in FcClient");
            }
        }
        
    }
}
