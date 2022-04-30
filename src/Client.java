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

public abstract class Client {

    private Socket s;
    private DataOutputStream dout;
    private BufferedReader din;
    private String reply;
    private String[] splitReply;
    private Boolean xmlFlag=false;

    private List<ServerObj> serverList=new ArrayList<ServerObj>();
    private ServerObj largest;
    private JobObj currentJob;


    //Setup the logger
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }
    private static final Logger logger=Logger.getLogger(LrrClient.class.getName());

    /**
     * Opens a new socket on the given hostname and port setsup the
     * din and dout,
     * then performs the handshake
     * @param hostname
     * @param port
     */
    void newConn(String hostname, int port){
        try{
            s=new Socket(hostname, port);
            dout=new DataOutputStream(s.getOutputStream());
            din=new BufferedReader(new InputStreamReader(s.getInputStream()));
            //HELO
            this.send("HELO\n");
            logger.log(Level.INFO,"RCVD: "+this.reply);
            //AUTH
            String username=System.getProperty("user.name");
            reply=this.send("AUTH "+username+"\n");
            logger.log(Level.INFO,"RCVD: "+reply);
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
            logger.log(Level.INFO,"RCVD: "+temp);
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
            logger.log(Level.INFO,"SENT: "+msg);
        } catch(IOException e){
            logger.log(Level.SEVERE,"ERR: "+e);
        }
	}

    public void getCapable(){

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

}
