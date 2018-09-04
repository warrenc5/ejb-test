package test.bean;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.Base64;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 * @author wozza
 */
public interface CallbackSessionBeanRemote extends EJBObject {

    void sendResponse(Object response) throws MyException;

    public static void sendResponse(Handle handle, Object response) throws MyException, RemoteException {
        ((CallbackSessionBeanRemote) handle.getEJBObject()).sendResponse(response);
    }

    public static Handle deserializeHandle(String string) throws IOException, ClassNotFoundException {
        Base64.Decoder b64 = Base64.getDecoder();
        byte[] decode = b64.decode(string);
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(decode));
        Handle handle = (Handle) in.readObject();
        return handle;
    }
}
