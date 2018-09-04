package test.bean;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author wozza
 */
@Stateful
@RemoteHome(value = CallbackSessionHome.class)
@LocalHome(value = CallbackSessionLocalHome.class)
public class CallbackSessionBean {

    private Logger logger = Logger.getLogger(CallbackSessionBean.class.getSimpleName());
    private String uuid;
    @Resource
    private SessionContext context;

    @Init
    public void ejbCreate(String uuid) {
        this.uuid = uuid;
    }

    public void ejbRemove() {
        try {
            SimpleFuture.lookupInEnvironment(uuid).cancel(true);
        } catch (NamingException ex) {
        }
    }

    @Asynchronous
    public void sendResponse(Object response) throws MyException {
        try {

            InitialContext initialContext = new InitialContext();
            Context c = (Context) initialContext.lookup(SimpleFuture.JNDI_BASE);
            SimpleFuture future = SimpleFuture.lookupInEnvironment(uuid);

            future.add(response);
        } catch (NamingException ex) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.log(Level.WARN, ex.getMessage(), ex);
            }
            throw new MyException(ex.getMessage());
        } catch (Throwable ex) {
            if (logger.isEnabledFor(Level.WARN)) {
                logger.log(Level.WARN, ex.getMessage(), ex);
            }
            throw new MyException(ex.getMessage());
        }
    }
}
