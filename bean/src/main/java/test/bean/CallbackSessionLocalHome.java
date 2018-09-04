/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.bean;

import javax.ejb.EJBLocalHome;

/**
 *
 * @author wozza
 */
public interface CallbackSessionLocalHome extends EJBLocalHome{

    CallbackSessionBeanLocal create(String uuid);
}
