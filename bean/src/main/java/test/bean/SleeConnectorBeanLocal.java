package test.bean;

import java.io.Serializable;
import java.util.concurrent.Future;

import javax.ejb.Local;
import javax.resource.ResourceException;
import javax.slee.Address;
import javax.slee.EventTypeID;
import javax.slee.UnrecognizedEventException;

@Local
public interface SleeConnectorBeanLocal {

    //public static final String NAME = "SleeConnectorBeanLocalName";
    //public static final String MAPPED_NAME = "SleeConnectorBeanLocalName/local";

    public EventTypeID lookupEventType(String name, String vendor, String version) throws UnrecognizedEventException;

    public void sendMessageToSlee(String eventName, String eventVendor, String eventVersion, Object eventObject,Address address) throws ResourceException, MyException;

    public void sendMessageToSlee(EventTypeID eventTypeID, Object eventObject,Address address) throws ResourceException, MyException;

    public Future sendMessageToSleeSynchronous(String eventName, String eventVendor, String eventVersion, Serializable eventObject) throws MyException;

    public Future sendMessageToSleeSynchronous(EventTypeID eventTypeID, Serializable eventObject) throws MyException;
}
