/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.bean;

import javax.ejb.EJBHome;

/**
 *
 * @author wozza
 */
public interface CallbackSessionHome extends EJBHome{

    CallbackSessionBeanRemote create(String uuid); 
}
