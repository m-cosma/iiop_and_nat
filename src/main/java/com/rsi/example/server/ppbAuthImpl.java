package com.rsi.example.server;

import com.rsi.example.generated.ppb;
import com.rsi.example.generated.ppbAuth;
import com.rsi.example.generated.ppbAuthHelper;
import com.rsi.example.generated.ppbAuthPackage.NoLogin;
import com.rsi.example.generated.ppbHelper;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class ppbAuthImpl extends com.rsi.example.generated.ppbAuthPOA {
    private static final Logger logger = LoggerFactory.getLogger(IiopNatServer.class);

    private static Integer currentId = 0;

    private HashMap<String, ppbImpl> activeObjects = new HashMap<>();

    @Override
    public ppb LogIn(String user, String password) throws NoLogin {
        ppb ppbRef = null;

        logger.trace("LogIn invoked with user={} and password=*****/{}", user, password);


        ppbImpl userObj = new ppbImpl();
        currentId++;
        String objectId = "id-" + currentId;
        logger.info("New internal object created: ", objectId);

        try {
            IiopNatServer.poa().activate_object_with_id(objectId.getBytes(), userObj);
            org.omg.CORBA.Object ref = IiopNatServer.poa().servant_to_reference(userObj);
            ppbRef = ppbHelper.narrow(ref);

            // TODO:
            //  No cleanup thread is implemented.
            //  You'll get memory leaks for objects that are not released with Logout.
            activeObjects.put(objectId, userObj);

        } catch (ServantNotActive | WrongPolicy | ServantAlreadyActive | ObjectAlreadyActive e) {
            logger.info("Stack trace:", e);
            logger.error("Caught exception: '{}'.", e.getMessage());
            throw new NoLogin(e.getMessage());
        }

        return ppbRef;
    }

    @Override
    public void LogOut(ppb session) throws NoLogin {
        logger.trace("LogOut invoked for session={}", session);

        if (session == null) {
            logger.error("Session is null.");
            return;
        }

        // get the objectId from the ppb object
        try {
            byte[] objectIdAsBytes = IiopNatServer.poa().reference_to_id(session);
            String objectId = new String(objectIdAsBytes, StandardCharsets.UTF_8);
            logger.info("Received logout for object: {}", objectId);
            ppbImpl userObj = activeObjects.get(objectId);
            if (userObj != null) {
                activeObjects.remove(objectId);
                logger.info("Successfully logged-out");
            } else {
                logger.error("Object not found in active objects.");
                throw new NoLogin("Session not found");
            }
        } catch (WrongAdapter | WrongPolicy e) {
            logger.info("Stack trace:", e);
            logger.error("Caught exception: '{}'.", e.getMessage());
            throw new NoLogin(e.getMessage());
        }
    }

    @Override
    public String GetVersion() {
        logger.trace("GetVersion invoked");

        return "test v7.8.9";
    }
}
