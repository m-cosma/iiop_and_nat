package com.rsi.example.client;

import com.rsi.example.common.IiopNatConst;
import com.rsi.example.generated.ppb;
import com.rsi.example.generated.ppbAuth;
import com.rsi.example.generated.ppbAuthHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StringHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IiopNatClient {
    private static final Logger logger = LoggerFactory.getLogger(IiopNatClient.class);

    private static ORB orb = null;

    public static void main(String[] args) {
        String host="";
        int port = 0;

        logger.info("Starting IiopNatClient...");

        if (args.length < 2) {
            logger.info("Usage: java IiopNatClient <ip> <port>");
            logger.error("Invalid application invocation. Exiting.");
            System.exit(1);
        }

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            logger.info("Usage: java IiopNatClient <ip> <port>");
            logger.error("Not a port number. {}", args[1], e.getMessage());
            System.exit(1);
        }

        // Make sure JacORB is used instead of Glassfish ORB
        System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        // Initialize ORB
        orb = ORB.init(args, null);
        logger.info("ORB implementation used: " + orb.getClass().getName());

        // Get a reference to the ppbAuth object (use corbaloc or specific endpoint)
        String corbaloc = "corbaloc:iiop:" + args[0] + ":" + args[1] + IiopNatConst.CORBA_PATH;
        logger.info("Going to connect to corba server: {}", corbaloc);
        org.omg.CORBA.Object serverObj = orb.string_to_object(corbaloc);

        // Narrow the object to the ppbAuth interface
        ppbAuth ppbAuthObj = ppbAuthHelper.narrow(serverObj);

        for (int loopId=0; loopId < 1000; loopId++) {
            logger.info("=========== Loop: {}", loopId);

            doCorbaCalls(ppbAuthObj);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.info("Stack trace:", e);
                logger.error("Caught exception: '{}'.", e.getMessage());
            }
        }

        logger.info("The End.");
    }

    private static void doCorbaCalls(ppbAuth ppbAuthObj) {
        ppb ppbObj = null;

        try {
            // GetVersion
            logger.info("   Version: {}", ppbAuthObj.GetVersion());

            // Login
            ppbObj = ppbAuthObj.LogIn("marius", "buhahaaa");
            logger.info("   Login successfull. Received reference: {}", orb.object_to_string(ppbObj));

            // GetMsisdn
            int ms_id = 199;
            StringHolder msisdn = new StringHolder();
            ppbObj.GetMsisdn(ms_id, msisdn);
            logger.info("      -> ms_id={}; msisdn={}", ms_id, msisdn.value);

        } catch (Exception e) {
            logger.info("Stack trace:", e);
            logger.error("Caught exception: '{}'.", e.getMessage());
        } finally {
            if (ppbObj != null) {
                try {
                    // Logout
                    ppbAuthObj.LogOut(ppbObj);
                    logger.info("   Logout successfull");
                } catch (Exception e) {
                    logger.info("Stack trace:", e);
                    logger.error("Caught exception: '{}'.", e.getMessage());
                }
            }
        }
    }
}
