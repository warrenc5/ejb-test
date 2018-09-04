package test.bean.ws;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jws.WebService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.EventTypeID;
import javax.slee.UnrecognizedEventException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature.Responses;
import test.bean.MyException;
import test.bean.SleeConnectorBeanLocal;

/**
 *
 * @author wozza
 */
@Stateless(name = "TestWebServiceSessionBean",
        mappedName = "TestWebServiceSessionName")
@Remote(TestWebServiceSessionBeanRemote.class)
//@WebContext(contextRoot = "/test-ws", urlPattern = "/test-service/", secureWSDLAccess = false)
@WebService(endpointInterface = "test.bean.TestWebServiceSessionBeanRemote",
        targetNamespace = "http://mofokom.eu/test/",
        serviceName = "TestService",
        portName = "TestPort")
@Addressing(enabled = true, required = true, responses = Responses.ANONYMOUS)
public class TestWebServiceSessionBean implements TestWebServiceSessionBeanRemote {

    public Logger logger = Logger.getLogger(TestWebServiceSessionBean.class.getName());
    static EventTypeID eventTypeID;

    @EJB(name = "SleeConnectorBean")
    protected SleeConnectorBeanLocal sleeConnector;

    private long timeout = 2000L;

    @Resource
    private WebServiceContext wcontext;

    @PostConstruct
    public void init() throws NamingException, UnrecognizedEventException {
        /*if (eventTypeID == null)
            eventTypeID = sleeConnector.lookupEventType("ExternalEvent", "Mofokom", "1.0");
            * 
         */
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object sendMessageToSleeWithCallback(String request) {
        /*for(Entry e : wcontext.getMessageContext().entrySet())
            logger.info(e.getKey().toString()  + " " + e.getValue());
            * 
         */
        return sendMessageToSlee(request);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object sendMessageToSlee(String request) {

        Future<Serializable> resultFuture = null;

        try {
            resultFuture = sleeConnector.sendMessageToSleeSynchronous(eventTypeID, request);

            Serializable result = resultFuture.get(timeout, TimeUnit.MILLISECONDS);

            if (result == null) {
                throw new RuntimeException("response was null");
            }

            return result;

        } catch (MyException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (TimeoutException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
        }
    }

    public static Context lookupEnvironment() throws NamingException {
        return (Context) new InitialContext().lookup("java:comp/env");
    }

    @Override
    public void noReturnMethod(String message) throws MyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
