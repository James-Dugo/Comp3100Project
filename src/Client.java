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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Client {

    public Boolean xmlFlag=false;
    public Socket s;
    public DataOutputStream dout;
    public BufferedReader din;
    public String reply;
    public String[] splitReply;
    public List<ServerObj> serverList=new ArrayList<ServerObj>();
    public JobObj currentJob;
    public String hostname;
    public int port;
    public Boolean verbose=true;

    //Setup the logger
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e1) {
            //e1.printStackTrace();
        }
    }
    public static final Logger logger=Logger.getLogger(LrrClient.class.getName());

    /**
     * Opens a new socket on the given hostname and port setsup the
     * din and dout,
     * then performs the handshake
     * @param hostname
     * @param port
     */
    public void newConn(String hostname, int port){
        try{
            this.s=new Socket(hostname, port);
            this.dout=new DataOutputStream(s.getOutputStream());
            this.din=new BufferedReader(new InputStreamReader(s.getInputStream()));
            //HELO
            this.send("HELO\n");
            //AUTH
            String username=System.getProperty("user.name");
            this.send("AUTH "+username+"\n");
        } catch(Exception e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }
    }

    /**
     * Sends a message and puts the reply into
     * this.reply
     * @param msg
     */
    public void send(String msg) {
        String temp;
        this.write(msg);
        temp=this.recieve();
        if(temp==""){
            logger.log(Level.SEVERE,"ERR: No reply, terminating"); 
            System.exit(1);}
        else{
            this.reply=temp;
        }
    }

    /**
     * reads a line in from din, 
     * handles the errors thrown by readLine()
     * Logs the received message
     * @return String the line read in as a String
     */
    public String recieve() {
        try{
            String temp=din.readLine();
            if(verbose)logger.log(Level.INFO,"RCVD: "+temp);
            return temp;
        }catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
            return "";
        }
	}

    /**
     * Sends a message to dout, 
     * handles the errors thrown by dout.write()
     * Logs what is sent
     * @param msg the message to be written
     */
	public void write(String msg) {
        try {
            dout.write(msg.getBytes());
            dout.flush();
            if(verbose)logger.log(Level.INFO,"SENT: "+msg);
        } catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }
	}

    /**
     * clear the current serverList
     * make an array of the servers recieved from a GETS Capable
     * store them in client.serverList
     */
    public void getCapable() {

        //clear the stale list
        this.serverList.clear();

        //send GETS Capable * * *(curr Job info)
        String[] msg=this.currentJob.capableMsg();
        this.send(String.format("GETS Capable %s %s %s\n",(Object[]) msg));

        //DATA >*< * is the num of lines
        int numLines=Integer.parseInt(this.reply.split(" ")[1]);

        //ready for the data
        this.write("OK\n");
        //Add servers to list
        for (int i = 0; i < numLines; i++) {
            this.reply=this.recieve();
            ServerObj server= new ServerObj(this.reply);
            this.serverList.add(server);
        }

        //finished Reading DATA * *
        this.send("OK\n");
        if(verbose)logger.log(Level.INFO, "RCVD: "+this.reply);
    }

    /**
     * sends a message SCHD jobId server serverid
     * @param jobId
     * @param server
     * @param serverid
     */
    public void schd(int jobId, String server, int serverid) {
        this.send(String.format("SCHD %d %s %d\n",jobId,server,serverid));
    }

    public void splitReply(){
        this.splitReply=this.reply.split(" ");
    }
    /**
     * closes the connection
     */
    public void cleanup() {
        this.send("QUIT\n");
        try {
            this.dout.close();
			this.din.close();
            this.s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        if(this.reply=="QUIT\n"){
            System.exit(0);
        }
    }

    /**
     * Reads the ds-system.xml file made by ds-server into a ServerObj class
     * and adds all of the types of servers to the client.serverList
     */
    public void readConfig() {
        try{
            //Read the file into a doc
            File file = new File("ds-system.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            //get the servers 
            NodeList list = doc.getElementsByTagName("server");
            //add each of the servers to the serverList
            for(int i=0;i<list.getLength();i++){
                Node node = list.item(i);
                if (node.getNodeType()==Node.ELEMENT_NODE){
                    NamedNodeMap nMap = node.getAttributes();
                    this.serverList.add(new ServerObj(nMap));
                }
            }
        }
        catch(Exception e){logger.log(Level.SEVERE, "ERR: "+e.getMessage());}
    }

    /**
     * This is what the main() calls to run the algorithm
     */
    abstract void runClient();
}
