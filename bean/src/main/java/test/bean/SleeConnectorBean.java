package test.bean;

import java.io.*;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import java.util.Properties;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.slee.Address;
import javax.slee.AddressPlan;
import javax.slee.EventTypeID;
import javax.slee.UnrecognizedEventException;
import javax.slee.connection.ExternalActivityHandle;
import javax.slee.connection.SleeConnection;
import javax.slee.connection.SleeConnectionFactory;
import sun.misc.BASE64Encoder;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Local(value = {SleeConnectorBeanLocal.class})
@Remote(value = {SleeConnectorBeanRemote.class})
public class SleeConnectorBean implements SleeConnectorBeanLocal {

    private transient SleeConnection connection = null;
    private Logger logger = Logger.getLogger(SleeConnectorBean.class.getSimpleName());
    private EventTypeID messageEventType;
    private static final String remoteMobicentsConnectionFactory = "java:/eis/SleeConnectionFactory";

    @Resource(mappedName = remoteMobicentsConnectionFactory)
    private SleeConnectionFactory factory;

    @Resource
    private EJBContext context;
    @EJB
    private CallbackSessionHome syncSessionHome;
    private Long asyncTimeout = 500L;

    public static Context lookupEnvironment() throws NamingException {
        return (Context) new InitialContext().lookup("java:comp/env");
    }

    public void closeSleeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (ResourceException ex) {
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn(null, ex);
                }
            } finally {
                connection = null;
            }
        }
    }

    @PostActivate
    @PostConstruct
    private void initializeSleeConnectionFactory() {
        if (factory == null) {
            logger.info("initializing");
            try {

                InitialContext ictx = new InitialContext();
                Properties props = new Properties();

                props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
                props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming rg.jnp.interfaces");

                if (logger.isInfoEnabled()) {
                    logger.info("lookup " + remoteMobicentsConnectionFactory);
                }

                factory = (SleeConnectionFactory) ictx.lookup(remoteMobicentsConnectionFactory);

                if (factory == null) {
                    throw new EJBException("No Factory Found  " + remoteMobicentsConnectionFactory);
                }

            } catch (NamingException ex) {
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn(ex.getMessage(), ex);
                }
                throw new EJBException(ex);
            }
        }
    }

    public EventTypeID lookupEventType(String name, String vendor, String version) {

        if (factory == null) {
            initializeSleeConnectionFactory();
        }

        try {
            connection = factory.getConnection();
            messageEventType = connection.getEventTypeID(name, vendor, version);

            if (logger.isDebugEnabled()) {
                logger.debug("event " + messageEventType);
            }

            return messageEventType;
        } catch (UnrecognizedEventException ex) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn(ex.getMessage(), ex);
            }
            throw new EJBException(ex);
        } catch (ResourceException ex) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn(ex.getMessage(), ex);
            }
            throw new EJBException(ex);
        } finally {
            closeSleeConnection();
        }
    }

    @Asynchronous
    public void sendMessageToSlee(String eventName, String eventVendor, String eventVersion, Object eventObject, Address address) throws MyException {
        EventTypeID eventTypeID = this.lookupEventType(eventName, eventVendor, eventVersion);
        sendMessageToSlee(eventTypeID, eventObject, address);
    }

    @Asynchronous
    public void sendMessageToSlee(EventTypeID eventTypeID, Object eventObject, Address address) throws MyException {

        try {
            connection = factory.getConnection();

            ExternalActivityHandle handle = connection.createActivityHandle();

            connection.fireEvent(eventObject, eventTypeID, handle, address);

            if (logger.isEnabledFor(Level.INFO)) {
                logger.info("event fired " + handle.toString() + " " + eventObject.getClass().getName());
            }
        } catch (Throwable x) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.warn(x.getMessage(), x);
            }
            throw new MyException(eventObject.toString());
        } finally {
            closeSleeConnection();
        }

    }

    @Timeout
    public void onTimeout(Timer timer) {
        Handle handle = (Handle) timer.getInfo();
        CallbackSessionBeanRemote syncSession;
        try {
            syncSession = (CallbackSessionBeanRemote) handle.getEJBObject();
            syncSession.remove();
        } catch (RemoveException | RemoteException ex) {
            java.util.logging.Logger.getLogger(SleeConnectorBean.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    public Future sendMessageToSleeSynchronous(EventTypeID eventTypeID, Serializable eventObject) throws MyException {

        try {

            SimpleFuture future = new SimpleFuture();

            CallbackSessionBeanRemote syncSession = syncSessionHome.create(future.getUuid());

            Handle handle = syncSession.getHandle();

            TimerService timerService = context.getTimerService();
            Timer timer = timerService.createTimer(asyncTimeout, handle);

            Address address = getAddress(handle);

            this.sendMessageToSlee(eventTypeID, eventObject, address);

            return future;
        } catch (NamingException ex) {
            throw new MyException(ex.getMessage());
        } catch (RemoteException ex) {
            throw new MyException(ex.getMessage());
        } catch (IOException ex) {
            throw new MyException(ex.getMessage());
        }
    }

    public Future sendMessageToSleeSynchronous(String eventName, String eventVendor, String eventVersion, Serializable eventObject) throws MyException {
        EventTypeID eventTypeID = this.lookupEventType(eventName, eventVendor, eventVersion);
        return sendMessageToSleeSynchronous(eventTypeID, eventObject);
    }

    public Address getAddress(Handle handle) throws RemoteException, IOException {
        Address address = new Address(AddressPlan.URI, serializeHandle(handle));
        return address;
    }

    public static Handle deserializeHandle(String string) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(string.getBytes());
        ObjectInputStream in = new ObjectInputStream(bais);
        Handle handle = (Handle) in.readObject();
        return handle;
    }

    public static String serializeHandle(Handle handle) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        ObjectOutputStream out = new ObjectOutputStream(pos);
        out.writeObject(handle);
        out.flush();

        BASE64Encoder b64 = new BASE64Encoder();
        b64.encode(pis, baos);

        return baos.toString();
    }
}
