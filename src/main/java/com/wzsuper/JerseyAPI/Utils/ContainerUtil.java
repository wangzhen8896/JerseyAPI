package com.wzsuper.JerseyAPI.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class ContainerUtil {
	
	public static String getAppPath(Class class1)
    {
        if(class1 == null)
            throw new IllegalArgumentException("getAppPath");
        ClassLoader classloader = class1.getClassLoader();
        String s = (new StringBuilder()).append(class1.getName()).append(".class").toString();
        Package package1 = class1.getPackage();
        String s1 = "";
        if(package1 != null)
        {
            String s2 = package1.getName();
            if(s2.startsWith("java.") || s2.startsWith("javax."))
                throw new IllegalArgumentException("getAppPath");
            s = s.substring(s2.length() + 1);
            if(s2.indexOf(".") < 0)
            {
                s1 = (new StringBuilder()).append(s2).append("/").toString();
            } else
            {
                int i = 0;
                boolean flag = false;
                for(int j = s2.indexOf("."); j != -1; j = s2.indexOf(".", i))
                {
                    s1 = (new StringBuilder()).append(s1).append(s2.substring(i, j)).append("/").toString();
                    i = j + 1;
                }

                s1 = (new StringBuilder()).append(s1).append(s2.substring(i)).append("/").toString();
            }
        }
        URL url = classloader.getResource((new StringBuilder()).append(s1).append(s).toString());
        String s3 = url.getPath();
        int k = s3.indexOf("file:");
        if(k > -1)
            s3 = s3.substring(k + 5);
        k = s3.indexOf((new StringBuilder()).append(s1).append(s).toString());
        s3 = s3.substring(0, k - 1);
        if(s3.endsWith("!"))
            s3 = s3.substring(0, s3.lastIndexOf("/"));
        try
        {
            s3 = URLDecoder.decode(s3, "utf-8");
        }
        catch(Exception exception)
        {
            throw new RuntimeException(exception);
        }
        return s3;
    }
	
	public static Integer getServerPort(){
		Integer port = 80;
	    String appName = Thread.currentThread().getContextClassLoader().getClass().getCanonicalName();
		String appPath = getAppPath(Thread.currentThread().getContextClassLoader().getClass());
	    SAXBuilder localSAXBuilder;
	    try
	    {
	      if (appName.equals("org.apache.catalina.loader.WebappClassLoader"))
	      {
              SAXBuilder saxbuilder1 = new SAXBuilder(false);
              FileInputStream fileinputstream1 = null;
              try
              {
                  File file = new File((new StringBuilder()).append(appPath).append("/../conf/server.xml").toString());
                  if(!file.exists())
                      file = new File((new StringBuilder()).append(appPath).append("/../../conf/server.xml").toString());
                  if(file.exists())
                  {
                      fileinputstream1 = new FileInputStream(file);
                      Document document1 = saxbuilder1.build(fileinputstream1);
                      List<Element> list1 = document1.getRootElement().getChild("Service").getChildren("Connector");
                      int i1 = 0;
                      do
                      {
                          if(i1 >= list1.size())
                              break;
                          Element element1 = (Element)list1.get(i1);
                          if(element1.getAttribute("protocol") == null || !element1.getAttributeValue("protocol").equals("AJP/1.3"))
                          {
                              port = Integer.parseInt((element1.getAttributeValue("port")));
                              break;
                          }
                          i1++;
                      } while(true);
                  }
              }
              catch(Exception exception1)
              {
                  exception1.printStackTrace();
              }
              finally
              {
                  if(fileinputstream1 != null)
                      try
                      {
                          fileinputstream1.close();
                      }
                      catch(IOException ioexception1)
                      {
                          ioexception1.printStackTrace();
                      }
              }
          }
	    }
	    catch (Throwable localThrowable2)
	    {
	    }
	    return port;
	}
}
