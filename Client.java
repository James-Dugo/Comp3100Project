
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
    private Boolean flag=false;

    private List<ServerObj> serverList=new ArrayList<ServerObj>();
    private ServerObj largest;
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

        //TODO set the client.flag by a command line arg
        //getting server info from ds-server.xml
        if(client.flag){
            client.readConfig();
            for(int i=0;i<client.serverList.size();i++){
                logger.log(Level.INFO,"SRVR type: "+client.serverList.get(i).getType());
            }
            client.pickLargest();
        }
        //getting server info from GETS
        else{
            //send ready and GETS
            client.ready();
            client.parseJob(client.reply.split(" "));
            client.getCapable();

            client.pickLargest();

            //for each server in the list, if its the same type as the largest increment the limit for the largest serverObj
            for (ServerObj server:client.serverList){
                if(server.getType().equals(client.largest.getType())){
                    client.largest.incrementLimit();
                }
            }
        }

        int i=0;
        int tmp=0;
        while(true){
            //REDY
            client.ready();
            client.splitReply(" ");
            if(client.splitReply[0].equals("JOBN")){
                client.parseJob(client.splitReply);

                client.reply=client.send(String.format("GETS Capable %d %d %d\n",client.jobCores,client.jobMem,client.jobDisk));
                client.splitReply(" ");
                tmp=Integer.parseInt(client.splitReply[1]);
                client.reply=client.send("OK\n");
                logger.log(Level.INFO, "RCVD: "+client.reply);
                client.splitReply(" ");
                for(int j=0;j<tmp-1;j++){logger.log(Level.INFO, "RCVD: "+client.recieve());}

                client.reply=client.send("OK\n");
                logger.log(Level.INFO, "RCVD: "+client.reply);

                client.schd(i);
                if(client.reply.equals("OK\n")){
                    logger.log(Level.SEVERE, "RCVD: "+client.reply+" Expected OK");
                }
                logger.log(Level.INFO, "RCVD: "+client.reply);

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
        File file = new File("ds-system.xml");
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
     * Send a REDY and put the reply into this.reply
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
        this.reply=this.send(String.format("SCHD %d %s %d\n",this.jobId,this.largest.getType(),serverid));
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
            ServerObj server= new ServerObj(reply);
            this.serverList.add(server);            
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
        String temp;
        this.write(msg);
        temp=this.recieve();
        if(temp!=null){return temp;}
        else{
            logger.log(Level.SEVERE,"ERR: No reply, terminating"); 
            System.exit(1);
            return null;
        }
    }
    
    /**
     * Takes a split JOBN reply and sets the client variables
     * @param reply
     */
    private void parseJob(String[] jobArr) {
        this.jobId=Integer.parseInt(jobArr[2]);
        this.jobCores=Integer.parseInt(jobArr[4]);
        this.jobMem=Integer.parseInt(jobArr[5]);
        this.jobDisk=Integer.parseInt(jobArr[6]);
    }
   
}