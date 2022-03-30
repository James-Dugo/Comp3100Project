package Comp3100Project;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client {
    private Socket s;
    private DataOutputStream dout;
    private BufferedReader din;
    private String reply;

    private List<String> serverList=new ArrayList<String>();
    private int jobId;
    private int jobCores;
    private int jobMem;
    private int jobDisk;

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }
    private static final Logger logger=Logger.getLogger(Client.class.getName());

    public static void main(String[] args){
        Client client=new Client();
        client.newConn("localhost",50000);
        String reply;

        //HELO
        reply=client.send("HELO\n");
        logger.log(Level.INFO,"RCVD: "+reply);

        //AUTH
        String username=System.getProperty("user.name");
        reply=client.send("AUTH "+username+"\n");
        logger.log(Level.INFO,"RCVD: "+reply);

        //REDY
        reply=client.send("REDY\n");
        logger.log(Level.INFO,"RCVD: "+reply);
        client.parseJob(reply);

        //GETS Capable
        reply=client.send(String.format("GETS Capable %d %d %d\n",client.jobCores,client.jobMem,client.jobDisk));
        client.write("OK\n");
        /*if(!reply.matches("^DATA*")){
            logger.log(Level.SEVERE,"RCVD something wrong fromm GETS");
            //TODO make this exit fix itself
            System.exit(1);
        } else{
            client.write("OK\n");
        }*/

        //Add servers to list
        reply=client.recieve();
        while(reply!=""){
            logger.log(Level.INFO, "RCVD: "+reply);
            client.serverList.add(reply);
            reply=client.recieve();
        }


        //TODO write cleanup
        client.cleanup();
    }

    /**
     * closes the connection and takes care of all that stuff
     */
    private void cleanup() {
        this.send("QUIT\n");
        System.exit(0);
    }

    /**
     * Opens a new socket on the given hostname and port and setsup the
     * din and dout
     * @param hostname
     * @param port
     */
    public void newConn(String hostname, int port){
        try{
            s=new Socket(hostname, port);
            dout=new DataOutputStream(s.getOutputStream());
            din=new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch(Exception e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }

    }
    
    /**
     * reads a line in from din
     * @return
     */
    private String recieve() {
        try{return din.readLine();}
        catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
            return null;
        }
	}
    
    /**
     * Sends a message to dout
     * @param msg
     */
	private void write(String msg) {
        try {
            dout.write(msg.getBytes());
            dout.flush();
        } catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }
	}
    
    /**
     * Sends a message and gets the reply from the connection, TODO make this less punishing if no reply exits
     * @param msg
     * @return reply
     */
    private String send(String msg){
        this.write(msg);
        reply=this.recieve();
        if(reply!=null){return reply;}
        else{
            logger.log(Level.SEVERE,"ERR: No reply, terminating"); 
            System.exit(1);
            return null;
        }
    }
    
    /**
     * Takes a JOBN reply and splits it, setting the client variables
     * @param reply
     */
    private void parseJob(String reply) {
        String[] jobArr = reply.split(" ");
        this.jobId=Integer.parseInt(jobArr[2]);
        this.jobCores=Integer.parseInt(jobArr[4]);
        this.jobMem=Integer.parseInt(jobArr[5]);
        this.jobDisk=Integer.parseInt(jobArr[6]);
    }

	
    
}