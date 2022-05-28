public class TtClient extends Client{

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
     * 3. 
     */
    public void mainLoop(){

    }
}
