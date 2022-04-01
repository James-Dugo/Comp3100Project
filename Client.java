
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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Client {
    private Socket s;
    private DataOutputStream dout;
    private BufferedReader din;
    private String reply;
    private String[] splitReply;

    private List<ServerObj> serverList=new ArrayList<ServerObj>();
    private ServerObj largest;
    private int jobId;
    private int jobCores;
    private int jobMem;
    private int jobDisk;

    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("/bin/main/logging.properties"));//TODO, easier to troubleshoot without logging file
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }
    private static final Logger logger=Logger.getLogger(Client.class.getName());

    public static void main(String[] args){
        Client client=new Client();
        client.newConn("localhost",50000);

        //read the ds-server.xml file
        client.readConfig();
        for(int i=0;i<client.serverList.size();i++){
            logger.log(Level.INFO,"SRVR type: "+client.serverList.get(i).getType());
        }
        client.pickLargest();
        int i=0;
        while(true){
            //REDY
            client.ready();
            client.splitReply(" ");
            if(client.splitReply[0].equals("JOBN")){
                client.parseJob(client.splitReply);
                client.schd(i);
                i++;
                if(i==client.largest.getLimit()){i=0;}
            }else if(client.splitReply[0].equals("NONE")){
                break;
            }
        }

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
     * Reads the ds-system.xml file made by ds-server into a ServerObj class
     * and adds all of the types of servers to the client.serverList
     */
    private void readConfig() {
        try{
        File file = new File("app/src/main/resources/ds-system.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();
        NodeList list = doc.getElementsByTagName("server");
        for(int i=0;i<list.getLength();i++){
            Node node = list.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE){
                NamedNodeMap nMap = node.getAttributes();
                this.serverList.add(new ServerObj(nMap));
            }
        }
        }

        //TODO figure out stack trace with logger
        catch(Exception e){logger.log(Level.SEVERE, "ERR: "+e.getMessage());}
    }

    /**
     * 
     */
    private void pickLargest() {
        this.largest=this.serverList.get(0);
        for (ServerObj server : this.serverList) {
            if(server.getCore()>this.largest.getCore()){
                this.largest=server;
            }
        }
    }

    /**
     * Send a REDY and deal with the reply
     * TODO make this handle things other than JOBN
     */
    private void ready() {
        this.reply=this.send("REDY\n");
        logger.log(Level.INFO,"RCVD: "+reply);
    }

    /**
     * 
     */
    private void splitReply(String exp) {
        this.splitReply=this.reply.split(exp);
    }

    /**
     * 
     */
    private void schd(int serverid) {
        this.send(String.format("SCHD %d %s %d\n",this.jobId,this.largest.getType(),serverid));
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
    private String send(String msg) {
        this.write(msg);
        this.reply=this.recieve();
        if(this.reply!=null){return this.reply;}
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