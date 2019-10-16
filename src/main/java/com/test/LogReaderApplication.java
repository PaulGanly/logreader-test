package com.test;

import com.test.services.LogReaderService;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;

public class LogReaderApplication {

    static Logger logger = Logger.getLogger(LogReaderApplication.class.getName());

    public static final Integer MAX_THREADS = 24;
    public static LogReaderService logReaderService = new LogReaderService();

    public static void main(String[] args) {
        logger.info("Received request to process logs with arguments: " + Arrays.toString(args));
        long startTime = System.currentTimeMillis();
        validateArguments(args);
        logReaderService.readLogFileAndStoreDetailsToDb(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        logger.info("Completed processing logs in " + (System.currentTimeMillis() - startTime) + " milliseconds");
    }

    /**
     * Check whether the user has specified the necessary arguments correctly.
     *
     * @param args
     */
    private static void validateArguments(String[] args) {
        if (args.length < 3) {
            logger.error("Not enough arguments provided. Arguments are: File Name/Path, Batch Size (Positive Integer - Number of rows in the file to process at once), " +
                    "Number of Threads (Positive Integer - Number of threads to process logs simultaneously)");
            System.exit(0);
        } else {
            if(!new File(args[0]).exists()){
                logger.error("Given file name does not exist");
                System.exit(0);
            }
            if(!isValidInteger(args[1]) || Integer.parseInt(args[1]) < 2){
                logger.error(args[1] + "Is not a valid argument for batch size. Batch size much be positive and has a minimum of 2");
                System.exit(0);
            }
            if(!isValidInteger(args[2]) || Integer.parseInt(args[1]) < 1 || Integer.parseInt(args[1]) < MAX_THREADS){
                logger.error(args[2] + "Is not a valid argument for thread number. Thread number must be positive and has a maximum of 24");
                System.exit(0);
            }
        }
    }

    public static boolean isValidInteger(String input) {
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }
}

