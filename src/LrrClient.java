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

public class LrrClient extends Client{


    public static void main(String[] args){
        LrrClient client=new LrrClient();
        client.newConn("localhost",50000);

        //TODO set the client.flag by a command line arg
        //getting server info from ds-server.xml
        if(this.xmlFlag){
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
            client.splitReply(" ");
            client.parseJob(client.splitReply);
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

            if(client.reply.equals(".\n")|client.splitReply[0].equals("JOBN")){
                client.parseJob(client.splitReply);

                //dont need to GETS if this is the first time after .getCapable()
                if(! client.reply.equals(".\n")){
                    //Gets Capable that dumps the info (just so that the server log is identical to ds-client)
                    client.reply=client.send(String.format("GETS Capable %d %d %d\n",client.jobCores,client.jobMem,client.jobDisk));
                    client.splitReply(" ");
                    tmp=Integer.parseInt(client.splitReply[1]);
                    client.reply=client.send("OK\n");
                    logger.log(Level.INFO, "RCVD: "+client.reply);
                    client.splitReply(" ");
                    for(int j=0;j<tmp-1;j++){logger.log(Level.INFO, "RCVD: "+client.recieve());}

                    client.reply=client.send("OK\n");
                    logger.log(Level.INFO, "RCVD: "+client.reply);
                }

                //schedule the job to server, client.largest id i
                client.schd(i);
                //If SCHD fails log SEVERE
                if(client.reply.equals("OK\n")){logger.log(Level.SEVERE, "RCVD: "+client.reply+" Expected OK");}
                //SCHD successful
                logger.log(Level.INFO, "RCVD: "+client.reply);

                //handles the round robbin part of lrr
                i++;
                if(i==client.largest.getLimit()){i=0;}

            }
            //if reply==!JOBN&reply==NONE we are done Scheduling
            else if(client.splitReply[0].equals("NONE")){
                break;
            }

            //Get the next message from the server
            client.ready();
            client.splitReply(" ");
        }

        client.cleanup();
    }



    /**
     * checks through the list of servers and sets this.largest
     * to the server with the most cores
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
     */
    private void ready() {
        this.reply=this.send("REDY\n");
        logger.log(Level.INFO,"RCVD: "+reply);
    }

    /**
     * Splits this.reply into this.splitReply
     * based on regex:exp
     * @param String exp
     */
    private void splitReply(String exp) {
        this.splitReply=this.reply.split(exp);
    }

    /**
     * schedules this.jobId to this.largest.getType(), serverid 
     * @param int serverid
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
     * closes the connection
     */
    private void cleanup() {
        reply=this.send("QUIT\n");
        if(reply=="QUIT\n"){
            System.exit(0);
        }
    }


    

    

    
    /**
     * Takes a split JOBN reply and sets the client variables
     * @param JobArr a strArr that looks like {"JOBN",...)
     */
    private void parseJob(String[] jobArr) {

    }
   
}