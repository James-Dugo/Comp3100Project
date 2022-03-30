package Comp3100Project;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class Client {
    private Socket s;
    private DataOutputStream dout;
    private BufferedReader din;
    private String reply;

    private List<ServerObj> serverList=new ArrayList<ServerObj>();
    private int jobId;
    private int jobCores;
    private int jobMem;
    private int jobDisk;

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }
    private static final Logger logger=Logger.getLogger(Client.class.getName());

    public static void main(String[] args){
        Client client=new Client();
        client.newConn("localhost",50000);
        String reply;

        //REDY
        client.ready();
        //read the ds-server.xml file
        client.getCapable();

        //LOOP
            //get the next job
            //get the capable servers for the job
            //schedule the job to the largest server Round robin style



        //TODO write cleanup
        client.cleanup();
    }

    /**
     * Opens a new socket on the given hostname and port and setsup the
     * din and dout, also performs the handshake
     * @param hostname
     * @param port
     */
    private void newConn(String hostname, int port){
        try{
            s=new Socket(hostname, port);
            dout=new DataOutputStream(s.getOutputStream());
            din=new BufferedReader(new InputStreamReader(s.getInputStream()));

            //HELO
            reply=this.send("HELO\n");
            logger.log(Level.INFO,"RCVD: "+reply);

            //AUTH
            String username=System.getProperty("user.name");
            reply=this.send("AUTH "+username+"\n");
            logger.log(Level.INFO,"RCVD: "+reply);

        } catch(Exception e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }

    }

    /**
     * Send a REDY and deal with the reply
     * TODO make this handle things other than JOBN
     */
    private void ready() {
        reply=this.send("REDY\n");
        logger.log(Level.INFO,"RCVD: "+reply);
        String[] jobArr = reply.split(" ");
        if(jobArr[0].equals("JOBN")){
            this.parseJob(jobArr);
        }else{
        }
    }

    /**
     * 
     */
    private void readConfig(){
        try{
        File file = new File("ds-system.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        }

        //TODO figure out stack trace with logger
        catch(Exception e){logger.log(Level.SEVERE, "ERR: "+e.getMessage());}
    }
    
    /**
     * make an array of the servers recieved from a GETS Capable
     * store them in client.serverList
     */
    private void getCapable() {

        //send GETS Capable * * *(curr Job info)
        reply=this.send(String.format("GETS Capable %d %d %d\n",this.jobCores,this.jobMem,this.jobDisk));

        //DATA >*< * is the num of lines
        int numLines=Integer.parseInt(reply.split(" ")[1]);

        //ready for the data
        this.write("OK\n");
        //Add servers to list
        for (int i = 0; i < numLines; i++) {
            reply=this.recieve();
            logger.log(Level.INFO, "RCVD: "+reply);
            this.serverList.add(new ServerObj(reply));
        }

        //finished Reading DATA * *
        reply=this.send("OK\n");
        logger.log(Level.INFO, "RCVD: "+reply);
    }

    /**
     * closes the connection and takes care of all that stuff
     */
    private void cleanup() {
        reply=this.send("QUIT\n");
        if(reply=="QUIT\n"){
            System.exit(0);
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
            logger.log(Level.INFO,"SENT: "+msg);
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
    private void parseJob(String[] jobArr) {
        this.jobId=Integer.parseInt(jobArr[2]);
        this.jobCores=Integer.parseInt(jobArr[4]);
        this.jobMem=Integer.parseInt(jobArr[5]);
        this.jobDisk=Integer.parseInt(jobArr[6]);
    }

	
    
}