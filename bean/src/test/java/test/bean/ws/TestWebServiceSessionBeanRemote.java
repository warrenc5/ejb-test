package test.bean.ws;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import test.bean.MyException;

/**
 * @author wozza
 */
@Remote
@WebService(name = "TestWebService",
        targetNamespace = "http://mofokom.eu/test/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.BARE)
public interface TestWebServiceSessionBeanRemote {

    @WebMethod(operationName = "noReturnOperation")
    void noReturnMethod(@WebParam String message) throws MyException;

    @WebMethod(operationName = "sendMessageToSlee", action = "")
    Object sendMessageToSlee(@WebParam String message) throws MyException;

    @WebMethod(operationName = "sendMessageToSleeWithCallback")
    Object sendMessageToSleeWithCallback(@WebParam String request) throws MyException;
}
