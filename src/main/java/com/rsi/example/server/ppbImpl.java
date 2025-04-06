package com.rsi.example.server;

import com.rsi.example.generated.ppbPackage.MethodFailure;
import org.omg.CORBA.StringHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ppbImpl extends com.rsi.example.generated.ppbPOA{
    private static final Logger logger = LoggerFactory.getLogger(IiopNatServer.class);

    @Override
    public void GetMsisdn(int ms_id, StringHolder msisdn) throws MethodFailure {
        logger.trace("GetMsisdn invoked with ms_id: {}", ms_id);
        if (ms_id == 0) {
            throw new MethodFailure((short)27, "ms_id is 0");
        }
        msisdn.value = "msisdn: 40729xx" + ms_id;
    }
}
