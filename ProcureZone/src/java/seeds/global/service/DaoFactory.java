/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.global.service;

import seeds.dbconfig.Util.HibernateUtil;
import seeds.dbconfig.Util.JDBC;
// TODO: Fix Hibernate classpath issues
// import org.hibernate.Session;
// import org.hibernate.SessionFactory;
// import org.hibernate.Transaction;
// import org.hibernate.cfg.Configuration;

/**
 *
 * @author ramesh.avv
 */
public class DaoFactory<T> {

    public static <T> T getDao(Class<T> daoClass) throws Exception {
        // TODO: Fix Hibernate classpath issues - temporary stub implementation
        try {
            return (T) daoClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot create DAO instance: " + ex.getMessage());
        }
    }

    /*
     * TODO: Restore when Hibernate classpath is fixed
     * private static DaoImplService getDAOByClass(Class c) throws Exception {
     * try {
     * Session s = getSession();
     * DaoImplService d = (DaoImplService) c.newInstance();
     * if (d.getSession() == null) {
     * d.setSession(s);
     * }
     * return d;
     * } catch (InstantiationException ex) {
     * } catch (IllegalAccessException ex) {
     * }
     * return null;
     * }
     * 
     * public static Session getSession() throws Exception {
     * Session
     * session=HibernateUtil.getSessionFactory().openSession(JDBC.getConnection());
     * return session;
     * }
     */
}
