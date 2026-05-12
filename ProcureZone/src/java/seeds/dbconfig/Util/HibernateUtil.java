/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.dbconfig.Util;

// Temporary stub - Hibernate imports commented out
// import org.hibernate.cfg.AnnotationConfiguration;
// import org.hibernate.SessionFactory;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author ramesh.avv
 */
public class HibernateUtil {

    // private static final SessionFactory sessionFactory;
    private static final Object sessionFactory;

    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml)
            // config file.
            // sessionFactory = new
            // AnnotationConfiguration().configure().buildSessionFactory();
            sessionFactory = new Object(); // Temporary stub
        } catch (Throwable ex) {
            // Log the exception.
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Object getSessionFactory() {
        return sessionFactory;
    }
}
