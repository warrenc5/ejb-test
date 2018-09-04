package test.bean;

import javax.ejb.ApplicationException;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.ws.WebFault;

/**
 *
 * @author wozza
 */
@WebFault(
  targetNamespace = "http://mofokom.eu/test/"
)
//WRECKS ##default namespace
@ApplicationException
public class MyException extends Exception {
    

    public MyException(Throwable cause) {
        super(cause);
    }

    public MyException(String message, Throwable cause) {
        super(message,cause);
    }

    public MyException(String message) {
        super(message);
    }

    public MyException() {
        super();
    }

    @Override
    @XmlTransient
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }
}