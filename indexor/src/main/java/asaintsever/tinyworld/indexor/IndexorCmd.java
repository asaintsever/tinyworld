package asaintsever.tinyworld.indexor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexorCmd {
    
    protected static Logger logger = LoggerFactory.getLogger(IndexorCmd.class);
    
    private static void usage() {
        System.out.println("Usage: " + IndexorCmd.class.getCanonicalName() + " <full path to ingest>\n");
        System.exit(1);
    }
    

    public static void main(String[] args) throws Exception {
        // Check args
        if (args.length != 1) usage();
        
        String ingestionPath = args[0];
        
        // Change defaults for our program
        Indexor.setClusterPathHome("target/index");
        Indexor.setIndex("indexor.cmd");
        
        // Create an indexor instance spawning an embedded cluster with expose set to 'true' to be able to connect to it with Elasticvue tool
        try(Indexor indexor = new Indexor(Indexor.DEFAULT_HOST, Indexor.DEFAULT_PORT, true, true)) {
            logger.info("Indexor Cmd started and ready to ingest photos from " + ingestionPath);
            
            
            Thread.sleep(8000);
        }
    }

}
