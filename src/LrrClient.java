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

    ServerObj largest;

    /**
     * checks through the list of servers and sets this.largest
     * to the server with the most cores
     */
    void pickLargest() {
        this.largest=this.serverList.get(0);
        for (ServerObj server : this.serverList) {
            if(server.getCore()>this.largest.getCore()){
                this.largest=server;
            }
        }
    }

    void mainLoop(){
        Boolean firstLoop=true;
        int currentServerId=0;

        loop:
        while(true){
            if(firstLoop){
                this.send("REDY/n");
                this.getCapable();
                this.pickLargest();
                firstLoop=false;
            }else{
                this.send("REDY/n");
            }
            swizzle:
            switch(this.reply){
                case("JOBN"):
                    this.getCapable();
                    schd(currentServerId);
                    if(currentServerId>this.largest.getLimit()){
                        currentServerId=0;
                    }else{
                        currentServerId++;
                    }
                    break swizzle;
                case("NONE"): break loop;
                default: break swizzle;
            }
        }
    }

    /**
     * schedules this.jobId to this.largest.getType(), serverid 
     * @param int serverid
     */
    void schd(int serverid) {
        this.send(String.format("SCHD %d %s %d\n",this.currentJob.getID(),this.largest.getType(),serverid));
    }
}