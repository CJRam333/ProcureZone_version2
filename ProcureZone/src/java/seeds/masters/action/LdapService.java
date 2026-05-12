package seeds.masters.action;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import pojo.TblLdapConfig;
import seeds.global.service.DaoFactory;
import seeds.masters.daoImpl.LdapConfigDaoImpl;

public class LdapService {
  private final LdapConfigDaoImpl ldap = (LdapConfigDaoImpl)DaoFactory.getDao(LdapConfigDaoImpl.class);
  TblLdapConfig ldapConfig;
  
  public LdapService()
    throws Exception
  {
    this.ldapConfig = new TblLdapConfig();
  }
  
  public boolean ADLdap(String email, String pwd)
    throws Exception
  {
    System.out.println("rama3");
    String[] domain = email.split("@");
    boolean b = false;
    if (domain.length > 0)
    {
      try
      {
        this.ldapConfig = ((TblLdapConfig)this.ldap.getList(" where configDomine ='nslindia.com'").get(0));
      }
      catch (Exception localException) {}
      String userName = this.ldapConfig.getConfigUser();
      String password = this.ldapConfig.getConfigPwd();
      
      Hashtable env = new Hashtable(11);
      env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
      
      env.put("java.naming.provider.url", this.ldapConfig.getConfigUrl());
      env.put("java.naming.security.authentication", "simple");
      env.put("java.naming.security.principal", "cn=" + this.ldapConfig.getConfigUser() + ",cn=users," + this.ldapConfig.getConfigPrinc());
      env.put("java.naming.security.credentials", this.ldapConfig.getConfigPwd());
      

      DirContext ctx = new InitialDirContext(env);
      env.clear();
      NamingEnumeration a = ctx.search("dc=nslgroup,dc=local", "mail=" + email + "", getSimpleSearchControls());
      ctx.close();
      int i = 0;
      if (a.hasMoreElements())
      {
        SearchResult result = (SearchResult)a.next();
        String cn = result.toString().split(":")[0];
        userName = cn;
        password = pwd;
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        
        env.put("java.naming.provider.url", this.ldapConfig.getConfigUrl());
        env.put("java.naming.security.authentication", "simple");
        env.put("java.naming.security.principal", userName + "," + this.ldapConfig.getConfigPrinc());
        env.put("java.naming.security.credentials", password);
        
        LdapContext ctx1 = new InitialLdapContext(env, null);
        b = true;
        ctx1.close();
        if (b) {
          b = true;
        } else {
          b = false;
        }
      }
    }
    return b;
  }
  
  public boolean OpenLdap(String email, String pwd)
  {
    boolean b = false;
    String[] domain = email.split("@");
    if (domain.length > 0)
    {
      try
      {
        this.ldapConfig = ((TblLdapConfig)this.ldap.getList(" where configDomine ='" + domain[1] + "'").get(0));
      }
      catch (Exception localException1) {}
      String username = domain[0];
      String password = pwd;
      Hashtable env = new Hashtable(11);
      try
      {
        String a = this.ldapConfig.getConfigPrinc();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        
        env.put("java.naming.provider.url", this.ldapConfig.getConfigUrl());
        env.put("java.naming.security.authentication", "simple");
        System.out.println("ldap config this"+a);
        env.put("java.naming.security.principal", "uid=" + username + "," + a);
        env.put("java.naming.security.credentials", password);
        try
        {
          DirContext ctx = new InitialDirContext(env);
          b = true;
        }
        catch (Exception e)
        {
          b = false;
        }
        finally
        {
          if (b) {
            b = true;
          } else {
            b = false;
          }
        }
      }
      catch (Exception e)
      {
        b = false;
      }
    }
    return b;
  }
  
  private static String replaceString(String text)
  {
    return text.replace("cn: ", "");
  }
  
  private static SearchControls getSimpleSearchControls()
  {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(2);
    searchControls.setTimeLimit(30000);
    

    return searchControls;
  }
}
