package com.rsi.example.server;

import com.rsi.example.generated.ppbAuth;
import com.rsi.example.generated.ppbAuthHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.rsi.example.common.IiopNatConst.*;

public class IiopNatServer {
    private static final Logger logger = LoggerFactory.getLogger(IiopNatServer.class);

    private static ORB orb = null;
    private static POA rsiPOA = null;

    public static void main(String[] args) {
        int port = 0;

        logger.info("Starting IiopNatServer application...");

        if (args.length < 1) {
            logger.info("Usage: java IiopNatServer <port> [<proxy_host>]");
            logger.error("Port number not specified. Exiting.");
            System.exit(1);
        }

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            logger.info("Usage: java IiopNatServer <port>");
            logger.error("Not a port number. {}", args[0], e.getMessage());
            System.exit(1);
        }

        // Set the properties for the server
        String[] orbArgs = new String[2];
        orbArgs[0] = "-ORBListenEndpoints";
        orbArgs[1] = "iiop://0.0.0.0:" + args[0];

        Properties props = new Properties();
        props.put("jacorb.implname", IMPL_NAME);
        if (args.length > 1) {
            props.put("jacorb.ior_proxy_host", args[1]);
            logger.info("Using IOR proxy host: {}", args[1]);
        }

        // Make sure JacORB is used instead of Glassfish ORB
        System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        try {
            // Initialize the ORB
            orb = ORB.init(orbArgs, props);
            logger.info("ORB implementation used: " + orb.getClass().getName());


            // Get the root POA (Portable Object Adapter)
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            // Create a new POA with the persistent lifespan policy.
            Policy p1 = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
            Policy p2 = rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
            rsiPOA = rootPOA.create_POA(POA_NAME, rootPOA.the_POAManager(), new Policy[]{p1, p2});


            // Activate POA manager
            rootPOA.the_POAManager().activate();

            // Create the servant (object implementation)
            ppbAuthImpl authImpl = new ppbAuthImpl();

            // Activate the servant
            rsiPOA.activate_object_with_id(SERVER_ID.getBytes(), authImpl);

            // Create a CORBA reference to the servant
            org.omg.CORBA.Object ref = rsiPOA.servant_to_reference(authImpl);
            logger.info("Server reference: '{}'", ref.toString());

//            ppbAuth ppbAuthObj = ppbAuthHelper.narrow(ref);

            // Bind the object reference directly in the server
            logger.info("Going to start corba server on port: {}", port);

            // Wait for client requests (ORB run loop)
            orb.run();
        } catch (Exception e) {
            logger.info("Stack trace:", e);
            logger.error("Caught exception: '{}'. Exiting.", e.getMessage());
        }


        logger.info("The End.");
    }

    public static ORB orb() {
        return orb;
    }

    public static POA poa() {
        return rsiPOA;
    }
}
